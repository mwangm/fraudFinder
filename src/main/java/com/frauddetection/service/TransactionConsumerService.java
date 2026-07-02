package com.frauddetection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.TransactionMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class TransactionConsumerService {

  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final FraudDetectionService detectionService;

  public TransactionConsumerService(
      ObjectMapper objectMapper, Validator validator, FraudDetectionService detectionService) {
    this.objectMapper = objectMapper;
    this.validator = validator;
    this.detectionService = detectionService;
  }

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
    detectionService.detect(input);
    log.info("SQS message processed: txnId={}", input.transactionId());
  }
}
