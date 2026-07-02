package com.frauddetection.service;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.Transaction;
import com.frauddetection.model.AlertEvent;
import com.frauddetection.model.TransactionMessage;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.rule.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FraudDetectionService {

  private final RuleEngine ruleEngine;
  private final FraudRecorderService fraudRecorder;
  private final TransactionRepository transactionRepo;
  private final ApplicationEventPublisher eventPublisher;

  public FraudDetectionService(
      RuleEngine ruleEngine,
      FraudRecorderService fraudRecorder,
      TransactionRepository transactionRepo,
      ApplicationEventPublisher eventPublisher) {
    this.ruleEngine = ruleEngine;
    this.fraudRecorder = fraudRecorder;
    this.transactionRepo = transactionRepo;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public void detect(TransactionMessage message) {
    if (transactionRepo.findById(message.transactionId()).isPresent()) {
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
              FraudRecord saved = fraudRecorder.save(message, evaluation);
              eventPublisher.publishEvent(new AlertEvent(saved));
            });
  }
}
