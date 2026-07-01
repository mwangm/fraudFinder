package com.frauddetection.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.frauddetection.model.DetectionResult;
import com.frauddetection.model.TransactionMessage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleEngineTest {

  RulesConfig properties;
  RuleEngine engine;

  @BeforeEach
  void setUp() {
    properties = new RulesConfig();
    properties.setThreshold(1);
    engine = new RuleEngine(properties);
  }

  @Test
  void givenAmountAboveThreshold_whenEvaluate_thenReturnsTriggered() {
    properties.setList(List.of(rule("big-amount", "amount > 5000", 40)));

    Optional<DetectionResult> eval = engine.evaluate(txn(10000));

    assertThat(eval).isPresent();
    assertThat(eval.get().ruleResults().get(0).score()).isEqualTo(40);
  }

  @Test
  void givenAmountBelowThreshold_whenEvaluate_thenReturnsEmpty() {
    properties.setList(List.of(rule("big-amount", "amount > 50000", 40)));

    Optional<DetectionResult> eval = engine.evaluate(txn(100));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenDisabledRule_whenEvaluate_thenSkipsRule() {
    properties.setList(
        List.of(new FraudDetectionRule("big-amount", "amount > 100", 40, "", false)));

    Optional<DetectionResult> eval = engine.evaluate(txn(10000));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenBlacklistedAccount_whenEvaluate_thenReturnsTriggered() {
    properties.setList(
        List.of(rule("blacklist", "{'ACC-BAD','ACC-FRAUD'}.contains(accountId)", 80)));

    Optional<DetectionResult> eval = engine.evaluate(txnWithAccount("ACC-BAD", 500));

    assertThat(eval).isPresent();
  }

  @Test
  void givenHighRiskPayee_whenEvaluate_thenReturnsTriggered() {
    properties.setList(List.of(rule("high-payee", "{'PE-HIGH'}.contains(payeeId)", 60)));

    Optional<DetectionResult> eval = engine.evaluate(txnWithPayee("PE-HIGH", 500));

    assertThat(eval).isPresent();
  }

  @Test
  void givenLowRiskPayee_whenEvaluate_thenReturnsEmpty() {
    properties.setList(List.of(rule("high-payee", "{'PE-HIGH'}.contains(payeeId)", 60)));

    Optional<DetectionResult> eval = engine.evaluate(txnWithPayee("PE-NORMAL", 500));

    assertThat(eval).isEmpty();
  }

  @Test
  void givenBadExpression_whenEvaluate_thenReturnsEmpty() {
    properties.setList(List.of(rule("bad", "nonexistentMethod()", 10)));

    Optional<DetectionResult> eval = engine.evaluate(txn(100));

    assertThat(eval).isEmpty();
  }

  private static FraudDetectionRule rule(String name, String condition, int score) {
    return new FraudDetectionRule(name, condition, score, "", true);
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
