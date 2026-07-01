package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import java.util.List;
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
  void givenFraudEvaluation_whenPublish_thenMessageContainsTransactionDetails() {
    var evaluation =
        new DetectionResult(
            "TXN-001",
            List.of(new DetectionResultDetail("付款方黑名单", 80, "Payer is blacklisted")),
            80,
            70,
            "2026-07-01T12:00:00Z");

    alertService.publish(evaluation);

    var captor = ArgumentCaptor.forClass(PublishRequest.class);
    verify(snsClient).publish(captor.capture());

    String message = captor.getValue().message();
    assertThat(message).contains("FRAUD ALERT!");
    assertThat(message).contains("Transaction ID: TXN-001");
    assertThat(message).contains("Score: 80/70");
    assertThat(message).contains("Detected At: 2026-07-01T12:00:00Z");
    assertThat(message).contains("Triggered Rules: 付款方黑名单(80)");
    assertThat(message).contains("- Payer is blacklisted");
  }

  @Test
  void givenNoTopicArn_whenPublish_thenDoesNotSend() {
    alertService = new AlertService(snsClient, "");
    var evaluation = new DetectionResult("TXN-001", List.of(), 80, 70, "2026-07-01T12:00:00Z");

    alertService.publish(evaluation);

    verify(snsClient, org.mockito.Mockito.never())
        .publish((PublishRequest) org.mockito.ArgumentMatchers.any());
  }
}
