package com.frauddetection.model;

import java.util.List;

public record DetectionResult(
    String transactionId,
    List<DetectionResultDetail> ruleResults,
    int totalScore,
    int threshold,
    String detectedAt) {}
