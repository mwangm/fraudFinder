package com.frauddetection.rule;

import com.frauddetection.message.EvaluationResult;
import com.frauddetection.message.RuleEvaluationResult;
import com.frauddetection.message.TransactionMessage;
import com.frauddetection.service.RiskCacheService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

@Service
public class RuleEngine {
  private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

  private final SpelExpressionParser parser = new SpelExpressionParser();
  private final RulesConfig rulesConfig;
  private final RiskCacheService riskCache;

  public RuleEngine(RulesConfig rulesConfig, RiskCacheService riskCache) {
    this.rulesConfig = rulesConfig;
    this.riskCache = riskCache;
  }
  public Optional<EvaluationResult> evaluate(TransactionMessage message) {
    EvaluationContext ctx = new StandardEvaluationContext(message);
    ctx.setVariable("riskCache", riskCache);

    List<RuleEvaluationResult> triggered = rulesConfig.getList().stream()
        .filter(FraudDetectionRule::enabled)
        .flatMap(rule -> evaluateRule(rule, ctx).stream())
        .toList();

    int totalScore = triggered.stream().mapToInt(RuleEvaluationResult::score).sum();

    int threshold = rulesConfig.getThreshold();
    return totalScore >= threshold
        ? Optional.of(new EvaluationResult(triggered, totalScore, threshold))
        : Optional.empty();
  }

  private Optional<RuleEvaluationResult> evaluateRule(FraudDetectionRule rule,
                                                       EvaluationContext ctx) {
    try {
      return Boolean.TRUE.equals(
              parser.parseExpression(rule.condition()).getValue(ctx, Boolean.class))
          ? Optional.of(RuleEvaluationResult.triggered(rule.name(), rule.score(), rule.reason()))
          : Optional.empty();
    } catch (Exception e) {
      log.error("Rule '{}' failed: {}", rule.name(), e.getMessage());
      return Optional.empty();
    }
  }
}
