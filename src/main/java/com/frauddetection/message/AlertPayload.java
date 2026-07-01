package com.frauddetection.message;

import java.util.List;

public record AlertPayload(
    String transactionId,
    int totalScore,
    int threshold,
    String detectedAt,
    List<String> triggeredRules) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String transactionId;
    private int totalScore;
    private int threshold;
    private String detectedAt;
    private List<String> triggeredRules;

    public Builder transactionId(String v) {
      this.transactionId = v;
      return this;
    }

    public Builder totalScore(int v) {
      this.totalScore = v;
      return this;
    }

    public Builder threshold(int v) {
      this.threshold = v;
      return this;
    }

    public Builder detectedAt(String v) {
      this.detectedAt = v;
      return this;
    }

    public Builder triggeredRules(List<String> v) {
      this.triggeredRules = v;
      return this;
    }

    public AlertPayload build() {
      return new AlertPayload(transactionId, totalScore, threshold, detectedAt, triggeredRules);
    }
  }
}
