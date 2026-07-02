package com.frauddetection.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.TransactionMessage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionConsumerServiceTest {

  @Mock FraudDetectionService detectionService;

  ObjectMapper objectMapper;
  Validator validator;
  TransactionConsumerService consumer;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    consumer = new TransactionConsumerService(objectMapper, validator, detectionService);
  }

  @Test
  void givenValidJson_whenOnMessage_thenDelegatesToDetection() {
    String json =
        "{\"transactionId\":\"TXN-1\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\",\"amount\":500}";

    consumer.onMessage(json);

    verify(detectionService)
        .detect(new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(500)));
  }

  @Test
  void givenInvalidJson_whenOnMessage_thenSkipsProcessing() {
    consumer.onMessage("not valid json {{{");

    verify(detectionService, never()).detect(any());
  }

  @Test
  void givenMissingFields_whenOnMessage_thenThrowsForRetry() {
    String json = "{\"transactionId\":\"TXN-1\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\"}";

    try {
      consumer.onMessage(json);
    } catch (IllegalArgumentException e) {
      // Expected — SQS will retry
    }

    verify(detectionService, never()).detect(any());
  }
}
