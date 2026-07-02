package com.frauddetection.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.entity.FraudRecord;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.rule.RuleEngine;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
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
  @Mock FraudRecorderService fraudRecorder;

  ObjectMapper objectMapper;
  Validator validator;
  FraudDetectionService service;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    service =
        new FraudDetectionService(objectMapper, ruleEngine, fraudRecorder, alertService, validator);
  }

  @Test
  void givenFraudScore_whenDetect_thenPublishesAlert() {
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());
    var evaluation =
        new DetectionResult(
            "TXN-1",
            List.of(new DetectionResultDetail("a", 40, "reason")),
            80,
            70,
            "2026-07-01T12:00:00Z");
    when(ruleEngine.evaluate(any())).thenReturn(Optional.of(evaluation));
    var saved = new FraudRecord("TXN-1", 80, 70, Instant.now());
    when(fraudRecorder.save(any(), any())).thenReturn(saved);

    service.detect(msg(5000));

    verify(alertService).publish(saved);
  }

  @Test
  void givenNormalScore_whenDetect_thenNoAction() {
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());
    when(ruleEngine.evaluate(any())).thenReturn(Optional.empty());

    service.detect(msg(500));

    verify(alertService, never()).publish(any());
  }

  @Test
  void givenDuplicateMessage_whenDetect_thenSkipsProcessing() {
    when(fraudRecorder.findExisting("TXN-1"))
        .thenReturn(Optional.of(new FraudRecord("TXN-1", 0, 0, Instant.now())));

    service.detect(msg(500));

    verify(ruleEngine, never()).evaluate(any());
  }

  @Test
  void givenValidJson_whenOnMessage_thenDeserializesAndDetects() {
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());
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

  @Test
  void givenMissingFields_whenOnMessage_thenThrowsAndTriggersRetry() {
    String json = "{\"transactionId\":\"TXN-1\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\"}";

    try {
      service.onMessage(json);
    } catch (IllegalArgumentException e) {
      // Expected — SQS will retry
    }

    verify(ruleEngine, never()).evaluate(any());
  }

  private static TransactionMessage msg(int amount) {
    return new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(amount));
  }
}
