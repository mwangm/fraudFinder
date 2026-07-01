package com.frauddetection.rule;

/**
 * A single fraud detection rule defined in YAML configuration. Each rule has a SpEL condition
 * evaluated against a Transaction, a score contributed when triggered, and a priority for ordering.
 */
public record FraudDetectionRule(
    String name, String condition, int score, int priority, boolean enabled) {

  /** Reason message when the rule triggers. */
  public String reason() {
    return name + " triggered (score=" + score + ")";
  }
}
