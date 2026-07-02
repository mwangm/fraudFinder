package com.frauddetection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.frauddetection.entity.Transaction;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class TransactionRepositoryTest {

  @Autowired TransactionRepository repo;

  @Test
  void givenTransaction_whenSave_thenCanFindById() {
    var txn = new Transaction("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(5000));
    repo.save(txn);

    Optional<Transaction> found = repo.findById("TXN-1");

    assertThat(found).isPresent();
    assertThat(found.get().getAmount()).isEqualByComparingTo("5000");
    assertThat(found.get().getCreatedAt()).isNotNull();
  }
}
