package com.frauddetection.model;

public record DetectionResultDetail(String ruleName, int score, String reason) {

  public static DetectionResultDetail triggered(String ruleName, int score, String reason) {
    return new DetectionResultDetail(ruleName, score, reason);
  }
}
