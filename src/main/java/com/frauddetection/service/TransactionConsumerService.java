package com.frauddetection.service;

import com.frauddetection.model.TransactionMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionConsumerService {

  private final FraudDetectionService detectionService;

  public TransactionConsumerService(FraudDetectionService detectionService) {
    this.detectionService = detectionService;
  }

  /**
   * Receives a transaction from SQS. JSON deserialization and bean validation are handled by Spring
   * Cloud AWS — failures trigger SQS retry and eventually dead-letter queue.
   */
  @SqsListener("${sqs.queue-name:fraud-transactions}")
  public void onMessage(@Payload @Valid TransactionMessage input) {
    log.info("SQS message received: txnId={}", input.transactionId());
    detectionService.detect(input);
    log.info("SQS message processed: txnId={}", input.transactionId());
  }
}
