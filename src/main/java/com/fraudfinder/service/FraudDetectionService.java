package com.fraudfinder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudfinder.message.TransactionMessage;
import com.fraudfinder.model.FraudRecord;
import com.fraudfinder.repository.FraudRecordRepository;
import com.fraudfinder.rule.RuleEngine;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudDetectionService {

  private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

  private final ObjectMapper objectMapper;
  private final RuleEngine ruleEngine;
  private final FraudRecordRepository resultRepo;
  private final FraudRecorderService fraudRecorder;
  private final AlertService alertService;

  public FraudDetectionService(
      ObjectMapper objectMapper,
      RuleEngine ruleEngine,
      FraudRecordRepository resultRepo,
      FraudRecorderService fraudRecorder,
      AlertService alertService) {
    this.objectMapper = objectMapper;
    this.ruleEngine = ruleEngine;
    this.resultRepo = resultRepo;
    this.fraudRecorder = fraudRecorder;
    this.alertService = alertService;
  }

  @SqsListener("${sqs.queue-name:fraud-transactions}")
  public void onMessage(String message) {
    if (message == null || message.isBlank()) {
      log.warn("Received empty SQS message, skipping");
      return;
    }

    try {
      TransactionMessage input = objectMapper.readValue(message, TransactionMessage.class);
      log.info("SQS message received: txnId={}", input.transactionId());
      FraudRecord result = detect(input);
      log.info(
          "SQS message processed: txnId={} fraudulent={}", input.transactionId(), result.isFraud());
    } catch (Exception e) {
      log.error("SQS message processing failed, will retry via visibility timeout", e);
      throw new RuntimeException("SQS processing failed", e);
    }
  }

  @Transactional
  public FraudRecord detect(TransactionMessage message) {
    Optional<FraudRecord> existing = resultRepo.findByTransactionId(message.transactionId());
    if (existing.isPresent()) {
      log.info("Idempotent skip: txnId={}", message.transactionId());
      return existing.get();
    }

    return ruleEngine
        .evaluate(message)
        .map(
            evaluation -> {
              FraudRecord result = fraudRecorder.save(message, evaluation);
              alertService.publish(result);
              return result;
            })
        .orElseGet(() -> new FraudRecord(message.transactionId(), false, 0, 0, Instant.now()));
  }
}
