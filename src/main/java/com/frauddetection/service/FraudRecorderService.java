package com.frauddetection.service;

import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.FraudRecord;
import com.frauddetection.model.FraudRecordDetail;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.FraudRecordRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FraudRecorderService {

  private final FraudRecordRepository resultRepo;

  public FraudRecorderService(FraudRecordRepository resultRepo) {
    this.resultRepo = resultRepo;
  }

  public Optional<FraudRecord> findExisting(String transactionId) {
    return resultRepo.findByTransactionId(transactionId);
  }

  @Transactional
  public FraudRecord save(TransactionMessage message, DetectionResult evaluation) {
    FraudRecord result =
        new FraudRecord(
            message.transactionId(),
            evaluation.totalScore(),
            evaluation.threshold(),
            Instant.now());

    evaluation
        .ruleResults()
        .forEach(
            rr ->
                result.addDetail(
                    new FraudRecordDetail(result, rr.ruleName(), rr.score(), rr.reason())));

    log.warn("FRAUD RECORDED: txnId={} score={}", message.transactionId(), evaluation.totalScore());
    return resultRepo.save(result);
  }
}
