package com.fraudfinder.detection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fraudfinder.message.*;
import com.fraudfinder.model.*;
import com.fraudfinder.model.FraudRecord;
import com.fraudfinder.repository.*;
import com.fraudfinder.service.*;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class FraudRecordRepositoryTest {

  @Autowired FraudRecordRepository repository;

  @Test
  void shouldFindByTransactionId() {
    FraudRecord r = new FraudRecord("TXN-001", true, 80, 70, Instant.now());
    repository.save(r);

    Optional<FraudRecord> found = repository.findByTransactionId("TXN-001");

    assertThat(found).isPresent();
    assertThat(found.get().isFraud()).isTrue();
    assertThat(found.get().getTotalScore()).isEqualTo(80);
  }

  @Test
  void shouldReturnEmptyForUnknownTransaction() {
    Optional<FraudRecord> found = repository.findByTransactionId("UNKNOWN");
    assertThat(found).isEmpty();
  }
}
