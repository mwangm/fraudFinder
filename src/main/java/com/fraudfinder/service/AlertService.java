package com.fraudfinder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudfinder.message.AlertPayload;
import com.fraudfinder.model.FraudRecord;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@ConditionalOnProperty(
    name = "spring.cloud.aws.sns.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AlertService {
  private static final Logger log = LoggerFactory.getLogger(AlertService.class);

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final String topicArn;

  public AlertService(
      SnsClient snsClient, ObjectMapper objectMapper, @Value("${sns.topic-arn:}") String topicArn) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.topicArn = topicArn;
  }

  public void publish(FraudRecord result) {
    if (topicArn.isBlank()) {
      log.info("SNS topic not configured, alert not sent: txnId={}", result.getTransactionId());
      return;
    }

    List<String> triggeredRuleNames =
        result.getDetails().stream()
            .filter(d -> d.isTriggered())
            .map(d -> d.getRuleName())
            .toList();

    var payload =
        AlertPayload.builder()
            .transactionId(result.getTransactionId())
            .totalScore(result.getTotalScore())
            .threshold(result.getThreshold())
            .detectedAt(result.getDetectedAt().toString())
            .triggeredRules(triggeredRuleNames)
            .build();

    try {
      String message = objectMapper.writeValueAsString(payload);
      snsClient.publish(
          PublishRequest.builder()
              .topicArn(topicArn)
              .subject("Fraud Alert: " + result.getTransactionId())
              .message(message)
              .build());
      log.info("Alert published to SNS: txnId={}", result.getTransactionId());
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize SNS alert: txnId={}", result.getTransactionId(), e);
    }
  }
}
