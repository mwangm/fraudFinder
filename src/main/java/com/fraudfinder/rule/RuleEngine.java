package com.fraudfinder.rule;

import com.fraudfinder.message.EvaluationResult;
import com.fraudfinder.message.RuleEvaluationResult;
import com.fraudfinder.message.TransactionMessage;
import com.fraudfinder.service.RiskCacheService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
  private final int threshold;

  public RuleEngine(
      RulesConfig rulesConfig,
      RiskCacheService riskCache,
      @Value("${fraud.detection.threshold:70}") int threshold) {
    this.rulesConfig = rulesConfig;
    this.riskCache = riskCache;
    this.threshold = threshold;
  }

  public Optional<EvaluationResult> evaluate(TransactionMessage message) {
    List<RuleEvaluationResult> results =
        rulesConfig.getList().stream()
            .filter(FraudDetectionRule::enabled)
            .map(rule -> evaluateRule(rule, message))
            .toList();

    int totalScore = results.stream().mapToInt(RuleEvaluationResult::score).sum();

    return totalScore >= threshold
        ? Optional.of(new EvaluationResult(results, totalScore, threshold))
        : Optional.empty();
  }

  private RuleEvaluationResult evaluateRule(FraudDetectionRule rule, TransactionMessage message) {
    try {
      EvaluationContext ctx = new StandardEvaluationContext(message);
      ctx.setVariable("riskCache", riskCache);
      return Boolean.TRUE.equals(
              parser.parseExpression(rule.condition()).getValue(ctx, Boolean.class))
          ? RuleEvaluationResult.triggered(rule.name(), rule.score(), rule.reason())
          : RuleEvaluationResult.notTriggered(rule.name());
    } catch (Exception e) {
      log.error(
          "Rule '{}' failed for txn {}: {}", rule.name(), message.transactionId(), e.getMessage());
      return RuleEvaluationResult.notTriggered(rule.name());
    }
  }
}
