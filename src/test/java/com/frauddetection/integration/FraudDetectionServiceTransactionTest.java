package com.frauddetection.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.frauddetection.entity.Transaction;
import com.frauddetection.repository.TransactionRepository;
import com.frauddetection.service.TransactionConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cloud.aws.sqs.enabled=false"})
@Transactional
class FraudDetectionServiceTransactionTest {

  @MockitoBean SnsClient snsClient;

  @Autowired TransactionConsumerService consumer;

  @Autowired TransactionRepository transactionRepo;

  @Test
  void givenValidMessage_whenOnMessage_thenTransactionIsSaved() {
    String json =
        "{\"transactionId\":\"TXN-001\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\",\"amount\":500}";

    consumer.onMessage(json);

    Transaction txn = transactionRepo.findById("TXN-001").orElseThrow();
    assertThat(txn.getAccountId()).isEqualTo("ACC-1");
    assertThat(txn.getAmount()).isEqualByComparingTo("500");
  }

  @Test
  void givenInvalidAmount_whenOnMessage_thenValidationFailsAndNoSave() {
    String json = "{\"transactionId\":\"TXN-002\",\"accountId\":\"ACC-1\",\"payeeId\":\"PE-1\"}";

    assertThatThrownBy(() -> consumer.onMessage(json)).isInstanceOf(IllegalArgumentException.class);

    assertThat(transactionRepo.findById("TXN-002")).isEmpty();
  }
}
