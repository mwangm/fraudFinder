package com.frauddetection.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.Transaction;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.rule.RuleEngine;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

  @Mock RuleEngine ruleEngine;
  @Mock FraudRecorderService fraudRecorder;
  @Mock TransactionRepository transactionRepo;
  @Mock ApplicationEventPublisher eventPublisher;

  FraudDetectionService service;

  @BeforeEach
  void setUp() {
    service = new FraudDetectionService(ruleEngine, fraudRecorder, transactionRepo, eventPublisher);
  }

  @Test
  void givenFraudScore_whenDetect_thenPublishesAlertEvent() {
    when(transactionRepo.findById(any())).thenReturn(Optional.empty());
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

    var captor = ArgumentCaptor.forClass(AlertEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    verify(fraudRecorder).save(any(), any());
  }

  @Test
  void givenNormalScore_whenDetect_thenNoEvent() {
    when(transactionRepo.findById(any())).thenReturn(Optional.empty());
    when(ruleEngine.evaluate(any())).thenReturn(Optional.empty());

    service.detect(msg(500));

    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void givenDuplicateMessage_whenDetect_thenSkipsProcessing() {
    when(transactionRepo.findById("TXN-1"))
        .thenReturn(Optional.of(new Transaction("TXN-1", "ACC-1", "PE-1", BigDecimal.ZERO)));

    service.detect(msg(500));

    verify(ruleEngine, never()).evaluate(any());
  }

  private static TransactionMessage msg(int amount) {
    return new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(amount));
  }
}
