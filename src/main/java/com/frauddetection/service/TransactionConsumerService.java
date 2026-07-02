package com.frauddetection.service;

import com.frauddetection.model.TransactionMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
public class TransactionConsumerService {

  private final FraudDetectionService detectionService;

  public TransactionConsumerService(FraudDetectionService detectionService) {
    this.detectionService = detectionService;
  }

  @SqsListener("${sqs.queue-name:fraud-transactions}")
  public void onMessage(@Payload @Valid TransactionMessage input) {
    log.info("SQS message received: txnId={}", input.transactionId());
    detectionService.detect(input);
    log.info("SQS message processed: txnId={}", input.transactionId());
  }
}
