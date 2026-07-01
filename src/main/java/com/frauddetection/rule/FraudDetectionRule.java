package com.frauddetection.rule;

/** A single fraud detection rule defined in YAML with a SpEL condition and a score. */
public record FraudDetectionRule(
    String name, String condition, int score, String alertMessage, boolean enabled) {}
