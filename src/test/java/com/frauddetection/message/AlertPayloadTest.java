package com.frauddetection.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertPayloadTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void givenPayload_whenSerialize_thenProducesJson() throws Exception {
    var payload = new AlertPayload("TXN-1", 80, 70, "2026-07-01T12:00:00Z", List.of("付款方黑名单"));

    String json = objectMapper.writeValueAsString(payload);

    assertThat(json).contains("TXN-1");
    assertThat(json).contains("totalScore");
    assertThat(json).contains("triggeredRules");
  }

  @Test
  void givenJson_whenDeserialize_thenReturnsPayload() throws Exception {
    String json = """
        {
          "transactionId": "TXN-1",
          "totalScore": 80,
          "threshold": 70,
          "detectedAt": "2026-07-01T12:00:00Z",
          "triggeredRules": ["付款方黑名单"]
        }
        """;

    AlertPayload payload = objectMapper.readValue(json, AlertPayload.class);

    assertThat(payload.transactionId()).isEqualTo("TXN-1");
    assertThat(payload.totalScore()).isEqualTo(80);
  }
}
