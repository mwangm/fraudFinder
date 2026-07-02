package com.frauddetection.repository;

import com.frauddetection.model.FraudRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudRecordRepository extends JpaRepository<FraudRecord, Long> {

  Optional<FraudRecord> findByTransactionId(String transactionId);
}
