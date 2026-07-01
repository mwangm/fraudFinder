package com.fraudfinder.message;

import java.util.List;

public record EvaluationResult(
    List<RuleEvaluationResult> ruleResults, int totalScore, int threshold) {}
