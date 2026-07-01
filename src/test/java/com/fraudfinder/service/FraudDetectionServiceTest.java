package com.fraudfinder.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudfinder.message.EvaluationResult;
import com.fraudfinder.message.RuleEvaluationResult;
import com.fraudfinder.message.TransactionMessage;
import com.fraudfinder.model.FraudRecord;
import com.fraudfinder.rule.RuleEngine;
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

  @Mock ObjectMapper objectMapper;
  @Mock RuleEngine ruleEngine;
  @Mock FraudRecorderService fraudRecorder;
  @Mock AlertService alertService;

  FraudDetectionService service;

  @BeforeEach
  void setUp() {
    service = new FraudDetectionService(objectMapper, ruleEngine, fraudRecorder, alertService);
  }

  @Test
  void shouldDetectFraudWhenScoreExceedsThreshold() {
    var evaluation =
        new EvaluationResult(
            List.of(RuleEvaluationResult.triggered("a", 40, ""), RuleEvaluationResult.triggered("b", 40, "")),
            80, 70);
    when(ruleEngine.evaluate(any())).thenReturn(Optional.of(evaluation));
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());

    service.detect(msg(5000));

    verify(fraudRecorder).save(any(), any());
    verify(alertService).publish(any());
  }

  @Test
  void shouldNotDetectFraudWhenScoreBelowThreshold() {
    when(ruleEngine.evaluate(any())).thenReturn(Optional.empty());
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());

    service.detect(msg(500));

    verify(fraudRecorder, never()).save(any(), any());
    verify(alertService, never()).publish(any());
  }

  @Test
  void shouldSkipDuplicateTransaction() {
    FraudRecord existing = new FraudRecord("TXN-1", false, 30, 70, Instant.now());
    when(fraudRecorder.findExisting("TXN-1")).thenReturn(Optional.of(existing));

    service.detect(msg(5000));

    verify(ruleEngine, never()).evaluate(any());
    verify(fraudRecorder, never()).save(any(), any());
  }

  @Test
  void shouldPublishAlertForFraud() {
    var evaluation = new EvaluationResult(List.of(RuleEvaluationResult.triggered("a", 120, "")), 120, 70);
    when(ruleEngine.evaluate(any())).thenReturn(Optional.of(evaluation));
    when(fraudRecorder.findExisting(any())).thenReturn(Optional.empty());

    service.detect(msg(5000));

    verify(alertService).publish(any());
  }

  private static TransactionMessage msg(int amount) {
    return new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(amount));
  }
}
