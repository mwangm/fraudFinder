package com.frauddetection.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class FraudDetectionIntegrationTest {

  private static final String QUEUE_NAME = "fraud-transactions-test";

  @Container
  static LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.3"))
          .withServices(LocalStackContainer.Service.SQS);

  static SqsClient sqsClient;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.cloud.aws.endpoint",
        () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS));
    registry.add("spring.cloud.aws.region.static", localstack::getRegion);
    registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
    registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");
  }

  @MockitoBean SnsClient snsClient;

  @Autowired TestAlertService alertService;

  @BeforeAll
  static void createQueue() {
    sqsClient =
        SqsClient.builder()
            .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
            .region(Region.of(localstack.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .build();
    sqsClient.createQueue(CreateQueueRequest.builder().queueName(QUEUE_NAME).build());
  }

  @AfterEach
  void tearDown() {
    alertService.reset();
    var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(QUEUE_NAME)).queueUrl();
    sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build());
  }

  @Test
  void givenFraudulentTransaction_whenSentToSqs_thenTriggersAlert() {
    var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(QUEUE_NAME)).queueUrl();

    sqsClient.sendMessage(
        SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(
                """
                {"transactionId":"ITX-001","accountId":"ACC-BAD","payeeId":"PE-1","amount":5000}
                """)
            .build());

    await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () -> {
              assertThat(alertService.getTriggeredRules()).isNotEmpty();
              assertThat(alertService.getTriggeredRules().get(0).ruleName())
                  .isEqualTo("suspicious-payer-account");
            });
  }

  @Test
  void givenNormalTransaction_whenSentToSqs_thenNoAlerts() {
    var queueUrl = sqsClient.getQueueUrl(r -> r.queueName(QUEUE_NAME)).queueUrl();

    sqsClient.sendMessage(
        SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(
                """
                {"transactionId":"ITX-002","accountId":"ACC-NORMAL","payeeId":"PE-1","amount":500}
                """)
            .build());

    await()
        .pollDelay(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              assertThat(alertService.getAlerts()).isEmpty();
            });
  }
}
