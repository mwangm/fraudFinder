package com.fraudfinder.detection.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.fraudfinder.message.*;
import com.fraudfinder.message.RuleEvaluationResult;
import com.fraudfinder.model.*;
import com.fraudfinder.repository.*;
import com.fraudfinder.service.*;
import org.junit.jupiter.api.Test;

class RuleEvaluationResultTest {

  @Test
  void triggeredShouldSetAllFields() {
    RuleEvaluationResult r = RuleEvaluationResult.triggered("test-rule", 50, "reason text");

    assertThat(r.triggered()).isTrue();
    assertThat(r.score()).isEqualTo(50);
    assertThat(r.reason()).isEqualTo("reason text");
  }

  @Test
  void notTriggeredShouldReturnZeroScoreAndEmptyReason() {
    RuleEvaluationResult r = RuleEvaluationResult.notTriggered("test-rule");

    assertThat(r.triggered()).isFalse();
    assertThat(r.score()).isEqualTo(0);
    assertThat(r.reason()).isEqualTo("");
  }
}
