package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.FraudRecord;
import com.frauddetection.model.FraudRecordDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.FraudRecordRepository;
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

@ExtendWith(MockitoExtension.class)
class FraudRecorderServiceTest {

  @Mock FraudRecordRepository repo;

  FraudRecorderService service;

  @BeforeEach
  void setUp() {
    service = new FraudRecorderService(repo);
  }

  @Test
  void givenNoExistingRecord_whenFindExisting_thenReturnsEmpty() {
    when(repo.findByTransactionId("TXN-1")).thenReturn(Optional.empty());

    Optional<FraudRecord> result = service.findExisting("TXN-1");

    assertThat(result).isEmpty();
  }

  @Test
  void givenExistingRecord_whenFindExisting_thenReturnsRecord() {
    var existing = new FraudRecord("TXN-1", 80, 70, Instant.now());
    when(repo.findByTransactionId("TXN-1")).thenReturn(Optional.of(existing));

    Optional<FraudRecord> result = service.findExisting("TXN-1");

    assertThat(result).isPresent();
    assertThat(result.get().getTransactionId()).isEqualTo("TXN-1");
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
}
