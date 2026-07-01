package com.fraudfinder.message;

public record RuleEvaluationResult(String ruleName, boolean triggered, int score, String reason) {

  public static RuleEvaluationResult notTriggered(String ruleName) {
    return new RuleEvaluationResult(ruleName, false, 0, "");
  }

  public static RuleEvaluationResult triggered(String ruleName, int score, String reason) {
    return new RuleEvaluationResult(ruleName, true, score, reason);
  }
}
