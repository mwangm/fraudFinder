package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.FraudRecordDetail;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

  @Mock SnsClient snsClient;

  AlertService alertService;

  @BeforeEach
  void setUp() {
    alertService = new AlertService(snsClient, "arn:aws:sns:test");
  }

  @Test
  void givenFraudRecord_whenPublish_thenMessageContainsTransactionDetails() {
    var record = new FraudRecord("TXN-001", 80, 70, Instant.now());
    record.addDetail(new FraudRecordDetail(record, "suspicious-payer", 80, "Payer is blacklisted"));

    alertService.publish(record);

    var captor = ArgumentCaptor.forClass(PublishRequest.class);
    verify(snsClient).publish(captor.capture());

    String message = captor.getValue().message();
    assertThat(message).contains("FRAUD DETECTED!");
    assertThat(message).contains("Transaction ID: TXN-001");
    assertThat(message).contains("Score: 80/70");
    assertThat(message).contains("suspicious-payer: Payer is blacklisted");
  }

  @Test
  void givenNoTopicArn_whenPublish_thenDoesNotSend() {
    alertService = new AlertService(snsClient, "");
    var record = new FraudRecord("TXN-001", 80, 70, Instant.now());

    alertService.publish(record);

    verify(snsClient, org.mockito.Mockito.never())
        .publish((PublishRequest) org.mockito.ArgumentMatchers.any());
  }

  @Test
  void givenSnsPublishFails_whenPublish_thenDoesNotThrow() {
    var record = new FraudRecord("TXN-001", 80, 70, Instant.now());
    when(snsClient.publish((PublishRequest) org.mockito.ArgumentMatchers.any()))
        .thenThrow(new RuntimeException("SNS unavailable"));

    // Should not throw — failures are logged and swallowed
    alertService.publish(record);
  }

  @Test
  void givenSendWithoutTopicArn_whenSend_thenDoesNotPublish() {
    alertService = new AlertService(snsClient, "");

    alertService.send("subject", "message");

    verify(snsClient, org.mockito.Mockito.never())
        .publish((PublishRequest) org.mockito.ArgumentMatchers.any());
  }
}
