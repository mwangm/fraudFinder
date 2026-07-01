package com.fraudfinder.repository;

import com.fraudfinder.model.FraudRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudRecordRepository extends JpaRepository<FraudRecord, Long> {

  Optional<FraudRecord> findByTransactionId(String transactionId);
}
