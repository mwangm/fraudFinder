package com.frauddetection.rule;

import lombok.extern.slf4j.Slf4j;

import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.model.TransactionMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

@Service

@Slf4j
public class RuleEngine {

  private final SpelExpressionParser parser = new SpelExpressionParser();
  private final RulesConfig rulesConfig;

  public RuleEngine(RulesConfig rulesConfig) {
    this.rulesConfig = rulesConfig;
  }

  public Optional<DetectionResult> evaluate(TransactionMessage message) {
    var ctx = new StandardEvaluationContext(message);

    List<DetectionResultDetail> triggered =
        rulesConfig.getList().stream()
            .filter(FraudDetectionRule::enabled)
            .flatMap(rule -> evaluateRule(rule, ctx).stream())
            .toList();

    int totalScore = triggered.stream().mapToInt(DetectionResultDetail::score).sum();

    int threshold = rulesConfig.getThreshold();
    return totalScore >= threshold
        ? Optional.of(
            new DetectionResult(
                message.transactionId(),
                triggered,
                totalScore,
                threshold,
                java.time.Instant.now().toString()))
        : Optional.empty();
  }

  private Optional<DetectionResultDetail> evaluateRule(
      FraudDetectionRule rule, StandardEvaluationContext ctx) {
    try {
      return Boolean.TRUE.equals(
              parser.parseExpression(rule.condition()).getValue(ctx, Boolean.class))
          ? Optional.of(
              DetectionResultDetail.triggered(rule.name(), rule.score(), rule.alertMessage()))
          : Optional.empty();
    } catch (Exception e) {
      log.error("Rule '{}' failed: {}", rule.name(), e.getMessage());
      return Optional.empty();
    }
  }
}
