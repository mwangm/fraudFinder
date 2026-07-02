package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.FraudRecordDetail;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.FraudRecordRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudRecorderServiceTest {

  @Mock FraudRecordRepository repo;

  FraudRecorderService service;

  @BeforeEach
  void setUp() {
    service = new FraudRecorderService(repo);
  }

  @Test
  void givenEvaluation_whenSave_thenPersistsRecordWithDetails() {
    var txn = new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(5000));
    var detail = new DetectionResultDetail("rule-1", 80, "reason");
    var evaluation = new DetectionResult("TXN-1", List.of(detail), 80, 70, "2026-07-01T12:00:00Z");
    var saved = new FraudRecord("TXN-1", 80, 70, Instant.now());
    saved.addDetail(new FraudRecordDetail(saved, "rule-1", 80, "reason"));
    when(repo.save(any())).thenReturn(saved);

    FraudRecord result = service.save(txn, evaluation);

    assertThat(result.getTransactionId()).isEqualTo("TXN-1");
    assertThat(result.getTotalScore()).isEqualTo(80);
    assertThat(result.getDetails()).hasSize(1);
    assertThat(result.getDetails().get(0).getRuleName()).isEqualTo("rule-1");

    var captor = ArgumentCaptor.forClass(FraudRecord.class);
    verify(repo).save(captor.capture());
    assertThat(captor.getValue().getDetails()).hasSize(1);
  }

  @Test
  void givenMultipleDetails_whenSave_thenPersistsAllDetails() {
    var txn = new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(5000));
    var d1 = new DetectionResultDetail("rule-1", 40, "reason-1");
    var d2 = new DetectionResultDetail("rule-2", 50, "reason-2");
    var evaluation = new DetectionResult("TXN-1", List.of(d1, d2), 90, 70, "2026-07-01T12:00:00Z");
    var saved = new FraudRecord("TXN-1", 90, 70, Instant.now());
    saved.addDetail(new FraudRecordDetail(saved, "rule-1", 40, "reason-1"));
    saved.addDetail(new FraudRecordDetail(saved, "rule-2", 50, "reason-2"));
    when(repo.save(any())).thenReturn(saved);

    FraudRecord result = service.save(txn, evaluation);

    assertThat(result.getDetails()).hasSize(2);
    verify(repo).save(any());
  }

  @Test
  void givenEmptyRuleResults_whenSave_thenPersistsRecordWithoutDetails() {
    var txn = new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(500));
    var evaluation = new DetectionResult("TXN-1", List.of(), 0, 70, "2026-07-01T12:00:00Z");
    var saved = new FraudRecord("TXN-1", 0, 70, Instant.now());
    when(repo.save(any())).thenReturn(saved);

    FraudRecord result = service.save(txn, evaluation);

    assertThat(result.getDetails()).isEmpty();
    verify(repo).save(any());
  }
}
