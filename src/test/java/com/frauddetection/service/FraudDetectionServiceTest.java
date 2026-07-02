package com.frauddetection.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.rule.RuleEngine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

  @Mock RuleEngine ruleEngine;
  @Mock AlertService alertService;

  ObjectMapper objectMapper;
  FraudDetectionService service;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    service = new FraudDetectionService(objectMapper, ruleEngine, alertService);
  }

  @Test
  void givenFraudScore_whenDetect_thenPublishesAlert() {
    var evaluation =
        new DetectionResult(
            "TXN-1",
            List.of(new DetectionResultDetail("a", 40, "reason")),
            80,
            70,
            "2026-07-01T12:00:00Z");
    when(ruleEngine.evaluate(any())).thenReturn(Optional.of(evaluation));

    service.detect(msg(5000));

    verify(alertService).publish(evaluation);
  }

  @Test
  void givenNormalScore_whenDetect_thenNoAction() {
    when(ruleEngine.evaluate(any())).thenReturn(Optional.empty());

    service.detect(msg(500));

    verify(alertService, never()).publish(any());
  }

  @Test
  void givenValidJson_whenOnMessage_thenDeserializesAndDetects() {
    when(ruleEngine.evaluate(any())).thenReturn(Optional.empty());
    String json =
        "{\"transactionId\":\"TXN-1\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\",\"amount\":500}";

    service.onMessage(json);

    verify(ruleEngine)
        .evaluate(new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(500)));
  }

  @Test
  void givenInvalidJson_whenOnMessage_thenSkipsProcessing() {
    service.onMessage("not valid json {{{");

    verify(ruleEngine, never()).evaluate(any());
  }

  private static TransactionMessage msg(int amount) {
    return new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(amount));
  }
}
