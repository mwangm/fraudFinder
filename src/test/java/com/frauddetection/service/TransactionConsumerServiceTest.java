package com.frauddetection.service;

import static org.mockito.Mockito.verify;

import com.frauddetection.model.TransactionMessage;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionConsumerServiceTest {

  @Mock FraudDetectionService detectionService;

  @Test
  void givenTransaction_whenOnMessage_thenDelegatesToDetection() {
    var consumer = new TransactionConsumerService(detectionService);
    var input = new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(500));

    consumer.onMessage(input);

    verify(detectionService)
        .detect(new TransactionMessage("TXN-1", "ACC-1", "PE-1", BigDecimal.valueOf(500)));
  }
}
