package com.frauddetection.service;

import com.frauddetection.entity.Transaction;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.rule.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FraudDetectionService {

  private final RuleEngine ruleEngine;
  private final FraudRecorderService fraudRecorder;
  private final AlertService alertService;
  private final TransactionRepository transactionRepo;

  public FraudDetectionService(
      RuleEngine ruleEngine,
      FraudRecorderService fraudRecorder,
      AlertService alertService,
      TransactionRepository transactionRepo) {
    this.ruleEngine = ruleEngine;
    this.fraudRecorder = fraudRecorder;
    this.alertService = alertService;
    this.transactionRepo = transactionRepo;
  }

  @Transactional
  public void detect(TransactionMessage message) {
    if (fraudRecorder.findExisting(message.transactionId()).isPresent()) {
      log.info("Already processed, skipping duplicate: txnId={}", message.transactionId());
      return;
    }

    try {
      transactionRepo.save(
          new Transaction(
              message.transactionId(), message.accountId(), message.payeeId(), message.amount()));
    } catch (DataIntegrityViolationException e) {
      log.warn("Duplicate transaction ignored: txnId={}", message.transactionId());
      return;
    }

    ruleEngine
        .evaluate(message)
        .ifPresent(
            evaluation -> {
              alertService.publish(fraudRecorder.save(message, evaluation));
            });
  }
}
