package com.frauddetection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.Transaction;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.rule.RuleEngine;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FraudDetectionService {

  private final ObjectMapper objectMapper;
  private final RuleEngine ruleEngine;
  private final FraudRecorderService fraudRecorder;
  private final AlertService alertService;
  private final Validator validator;
  private final TransactionRepository transactionRepo;

  public FraudDetectionService(
      ObjectMapper objectMapper,
      RuleEngine ruleEngine,
      FraudRecorderService fraudRecorder,
      AlertService alertService,
      Validator validator,
      TransactionRepository transactionRepo) {
    this.objectMapper = objectMapper;
    this.ruleEngine = ruleEngine;
    this.fraudRecorder = fraudRecorder;
    this.alertService = alertService;
    this.validator = validator;
    this.transactionRepo = transactionRepo;
  }

  @Transactional
  @SqsListener("${sqs.queue-name:fraud-transactions}")
  public void onMessage(String message) {
    TransactionMessage input;
    try {
      input = objectMapper.readValue(message, TransactionMessage.class);
    } catch (JsonProcessingException e) {
      log.error("Invalid SQS message, skipping: {}", message, e);
      return;
    }

    Set<ConstraintViolation<TransactionMessage>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      String errors =
          violations.stream()
              .map(v -> v.getPropertyPath() + " " + v.getMessage())
              .collect(Collectors.joining(", "));
      log.error("SQS message validation failed: txnId={} errors={}", input.transactionId(), errors);
      throw new IllegalArgumentException("Invalid transaction message: " + errors);
    }

    log.info("SQS message received: txnId={}", input.transactionId());
    detect(input);
    log.info("SQS message processed: txnId={}", input.transactionId());
  }

  void detect(TransactionMessage message) {
    if (fraudRecorder.findExisting(message.transactionId()).isPresent()) {
      log.info("Already processed, skipping duplicate: txnId={}", message.transactionId());
      return;
    }

    Optional<DetectionResult> evaluation = ruleEngine.evaluate(message);

    transactionRepo.save(
        new Transaction(
            message.transactionId(), message.accountId(), message.payeeId(), message.amount()));

    if (evaluation.isPresent()) {
      FraudRecord save = fraudRecorder.save(message, evaluation.get());
      alertService.publish(save);
    }
  }
}
