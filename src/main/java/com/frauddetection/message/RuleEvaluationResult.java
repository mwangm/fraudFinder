package com.frauddetection.message;

public record RuleEvaluationResult(String ruleName, int score, String reason) {

  public static RuleEvaluationResult triggered(String ruleName, int score, String reason) {
    return new RuleEvaluationResult(ruleName, score, reason);
  }
}
