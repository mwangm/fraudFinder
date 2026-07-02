package com.frauddetection.service;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.FraudRecordDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@Slf4j
public class AlertService {

  private final SnsClient snsClient;
  private final String topicArn;

  public AlertService(SnsClient snsClient, @Value("${sns.topic-arn:}") String topicArn) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAlertEvent(AlertEvent event) {
    publish(event.fraudRecord());
  }

  public void publish(FraudRecord result) {
    if (topicArn.isBlank()) {
      log.warn("SNS topic ARN not configured, skipping alert: txnId={}", result.getTransactionId());
      return;
    }

    var msg =
        new StringBuilder()
            .append("FRAUD DETECTED!\n")
            .append("Transaction ID: ")
            .append(result.getTransactionId())
            .append("\n")
            .append("Score: ")
            .append(result.getTotalScore())
            .append("/")
            .append(result.getThreshold())
            .append("\n")
            .append("Detected At: ")
            .append(result.getDetectedAt())
            .append("\n");

    for (FraudRecordDetail d : result.getDetails()) {
      msg.append("- ").append(d.getRuleName()).append(": ").append(d.getReason()).append("\n");
    }

    try {
      snsClient.publish(
          PublishRequest.builder()
              .topicArn(topicArn)
              .subject("Fraud Alert: " + result.getTransactionId())
              .message(msg.toString())
              .build());
      log.info("Alert published to SNS: txnId={}", result.getTransactionId());
    } catch (Exception ex) {
      log.error("Failed to publish SNS alert: txnId={}", result.getTransactionId(), ex);
    }
  }
}
