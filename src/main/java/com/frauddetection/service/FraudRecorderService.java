package com.frauddetection.service;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.FraudRecordDetail;
import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.FraudRecordRepository;
import java.time.Instant;
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

  /**
   * Persists a fraud detection result with all triggered rule details.
   *
   * @return the saved fraud record
   */
  @Transactional
  public FraudRecord save(TransactionMessage message, DetectionResult evaluation) {
    Instant detectedAt;
    try {
      detectedAt = Instant.parse(evaluation.detectedAt());
    } catch (Exception e) {
      log.warn(
          "Failed to parse detectedAt '{}', falling back to Instant.now()",
          evaluation.detectedAt());
      detectedAt = Instant.now();
    }

    FraudRecord result =
        new FraudRecord(
            message.transactionId(), evaluation.totalScore(), evaluation.threshold(), detectedAt);

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
