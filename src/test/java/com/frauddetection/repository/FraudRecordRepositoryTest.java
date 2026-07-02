package com.frauddetection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.frauddetection.entity.FraudRecord;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class FraudRecordRepositoryTest {

  @Autowired FraudRecordRepository repo;

  @Test
  void givenFraudRecord_whenSave_thenCanFindByTransactionId() {
    var record = new FraudRecord("TXN-1", 80, 70, Instant.now());
    repo.save(record);

    Optional<FraudRecord> found = repo.findByTransactionId("TXN-1");

    assertThat(found).isPresent();
    assertThat(found.get().getTotalScore()).isEqualTo(80);
  }

  @Test
  void givenNoFraudRecord_whenFind_thenEmpty() {
    Optional<FraudRecord> found = repo.findByTransactionId("NONEXISTENT");

    assertThat(found).isEmpty();
  }
}
