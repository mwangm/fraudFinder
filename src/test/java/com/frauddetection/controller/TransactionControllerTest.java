package com.frauddetection.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.FraudRecordDetail;
import com.frauddetection.entity.Transaction;
import com.frauddetection.repository.FraudRecordRepository;
import com.frauddetection.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

  @Mock TransactionRepository transactionRepo;
  @Mock FraudRecordRepository fraudRecordRepo;

  @InjectMocks TransactionController controller;

  @Test
  void givenExistingTransaction_whenGetById_thenReturnsOk() {
    when(transactionRepo.findById("TXN-1"))
        .thenReturn(
            Optional.of(new Transaction("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(5000))));
    when(fraudRecordRepo.findByTransactionId("TXN-1")).thenReturn(Optional.empty());

    var response = controller.getTransaction("TXN-1");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody().transactionId()).isEqualTo("TXN-1");
    assertThat(response.getBody().fraud()).isFalse();
  }

  @Test
  void givenNotFound_whenGetById_thenReturns404() {
    when(transactionRepo.findById("NONEXISTENT")).thenReturn(Optional.empty());

    var response = controller.getTransaction("NONEXISTENT");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
  }

  @Test
  void givenFraudTransaction_whenGetById_thenReturnsWithFraudDetails() {
    when(transactionRepo.findById("TXN-FRAUD"))
        .thenReturn(
            Optional.of(
                new Transaction("TXN-FRAUD", "ACC-BAD", "PE-HIGH", BigDecimal.valueOf(5000))));
    var fraudRecord = new FraudRecord("TXN-FRAUD", 80, 70, Instant.now());
    fraudRecord.addDetail(
        new FraudRecordDetail(fraudRecord, "suspicious-payer", 80, "blacklisted"));
    when(fraudRecordRepo.findByTransactionId("TXN-FRAUD")).thenReturn(Optional.of(fraudRecord));

    var response = controller.getTransaction("TXN-FRAUD");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody().fraud()).isTrue();
    assertThat(response.getBody().fraudDetails()).hasSize(1);
    assertThat(response.getBody().fraudDetails().get(0).ruleName()).isEqualTo("suspicious-payer");
  }
}
