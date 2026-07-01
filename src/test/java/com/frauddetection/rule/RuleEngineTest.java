package com.frauddetection.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frauddetection.message.EvaluationResult;
import com.frauddetection.message.TransactionMessage;
import com.frauddetection.model.PayeeRisk;
import com.frauddetection.rule.FraudDetectionRule;
import com.frauddetection.rule.RuleEngine;
import com.frauddetection.rule.RulesConfig;
import com.frauddetection.service.RiskCacheService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

  @Mock RiskCacheService riskCache;

  RulesConfig properties;
  RuleEngine engine;

  @BeforeEach
  void setUp() {
    properties = new RulesConfig();
    properties.setThreshold(1);
    engine = new RuleEngine(properties, riskCache);
  }

  @Test
  void givenAmountAboveThreshold_whenEvaluate_thenReturnsTriggered() {
    properties.setList(List.of(new FraudDetectionRule("big-amount", "amount > 5000", 40, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txn(10000));

    assertThat(eval).isPresent();
    assertThat(eval.get().ruleResults().get(0).score()).isEqualTo(40);
  }

  @Test
  void givenAmountBelowThreshold_whenEvaluate_thenReturnsEmpty() {
    properties.setList(List.of(new FraudDetectionRule("big-amount", "amount > 50000", 40, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txn(100));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenDisabledRule_whenEvaluate_thenSkipsRule() {
    properties.setList(List.of(new FraudDetectionRule("big-amount", "amount > 100", 40, false)));

    Optional<EvaluationResult> eval = engine.evaluate(txn(10000));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenBlacklistedAccount_whenEvaluate_thenReturnsTriggered() {
    when(riskCache.isSuspicious("ACC-BAD")).thenReturn(true);
    properties.setList(List.of(
        new FraudDetectionRule("blacklist", "#riskCache.isSuspicious(accountId)", 80, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txnWithAccount("ACC-BAD", 500));

    assertThat(eval).isPresent();
  }

  @Test
  void givenHighRiskPayee_whenEvaluate_thenReturnsTriggered() {
    when(riskCache.getPayeeRisk("PE-HIGH"))
        .thenReturn(new PayeeRisk("PE-HIGH", "HIGH", "bad history", Instant.now()));
    properties.setList(List.of(
        new FraudDetectionRule("high-payee",
            "#riskCache.getPayeeRisk(payeeId)?.riskLevel == 'HIGH'", 60, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txnWithPayee("PE-HIGH", 500));

    assertThat(eval).isPresent();
  }

  @Test
  void givenLowRiskPayee_whenEvaluate_thenReturnsEmpty() {
    when(riskCache.getPayeeRisk("PE-NORMAL"))
        .thenReturn(new PayeeRisk("PE-NORMAL", "LOW", "clean", Instant.now()));
    properties.setList(List.of(
        new FraudDetectionRule("high-payee",
            "#riskCache.getPayeeRisk(payeeId)?.riskLevel == 'HIGH'", 60, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txnWithPayee("PE-NORMAL", 500));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenBadExpression_whenEvaluate_thenReturnsEmpty() {
    properties.setList(List.of(new FraudDetectionRule("bad", "nonexistentMethod()", 10, true)));

    Optional<EvaluationResult> eval = engine.evaluate(txn(100));

    assertThat(eval).isEmpty();
  }

  private static TransactionMessage txn(int amount) {
    return txnWithAccount("ACC-1", amount);
  }

  private static TransactionMessage txnWithAccount(String accountId, int amount) {
    return new TransactionMessage("TXN-1", accountId, "PE-1", BigDecimal.valueOf(amount));
  }

  private static TransactionMessage txnWithPayee(String payeeId, int amount) {
    return new TransactionMessage("TXN-1", "ACC-1", payeeId, BigDecimal.valueOf(amount));
  }
}
