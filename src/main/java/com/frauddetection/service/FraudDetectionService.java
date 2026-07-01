package com.frauddetection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.rule.RuleEngine;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

  private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

  private final ObjectMapper objectMapper;
  private final RuleEngine ruleEngine;
  private final AlertService alertService;

  public FraudDetectionService(
      ObjectMapper objectMapper, RuleEngine ruleEngine, AlertService alertService) {
    this.objectMapper = objectMapper;
    this.ruleEngine = ruleEngine;
    this.alertService = alertService;
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

    log.info("SQS message received: txnId={}", input.transactionId());
    detect(input);
    log.info("SQS message processed: txnId={}", input.transactionId());
  }

  void detect(TransactionMessage message) {
    ruleEngine.evaluate(message).ifPresent(alertService::publish);
  }
}
