package com.fraudfinder.service;

import com.fraudfinder.message.EvaluationResult;
import com.fraudfinder.message.TransactionMessage;
import com.fraudfinder.model.FraudRecord;
import com.fraudfinder.model.FraudRecordDetail;
import com.fraudfinder.repository.FraudRecordRepository;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudRecorderService {

  private static final Logger log = LoggerFactory.getLogger(FraudRecorderService.class);

  private final FraudRecordRepository resultRepo;

  public FraudRecorderService(FraudRecordRepository resultRepo) {
    this.resultRepo = resultRepo;
  }

  public Optional<FraudRecord> findExisting(String transactionId) {
    return resultRepo.findByTransactionId(transactionId);
  }

  @Transactional
  public FraudRecord save(TransactionMessage message, EvaluationResult evaluation) {
    FraudRecord result =
        new FraudRecord(
            message.transactionId(), evaluation.totalScore(), evaluation.threshold(), Instant.now());

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
