package com.frauddetection.service;

import com.frauddetection.message.DetectionResult;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class AlertService {

  private static final Logger log = LoggerFactory.getLogger(AlertService.class);

  private final SnsClient snsClient;
  private final String topicArn;

  public AlertService(SnsClient snsClient, @Value("${sns.topic-arn:}") String topicArn) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
  }

  public void publish(DetectionResult e) {
    if (topicArn.isBlank()) return;

    snsClient.publish(
        PublishRequest.builder()
            .topicArn(topicArn)
            .subject("Fraud Alert: " + e.transactionId())
            .message(buildMessage(e))
            .build());

    log.info("Alert published to SNS: txnId={}", e.transactionId());
  }

  private String buildMessage(DetectionResult e) {
    var triggered =
        e.ruleResults().stream()
            .map(r -> r.ruleName() + "(" + r.score() + ")")
            .collect(Collectors.joining(", "));

    var details =
        e.ruleResults().stream().map(r -> "- " + r.reason()).collect(Collectors.joining("\n"));

    return """
        FRAUD ALERT!
        Transaction ID: %s
        Score: %d/%d
        Detected At: %s
        Triggered Rules: %s
        Details:
        %s"""
        .formatted(
            e.transactionId(), e.totalScore(), e.threshold(), e.detectedAt(), triggered, details);
  }
}
