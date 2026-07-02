package com.frauddetection.rule;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fraud.rules")
public class RulesConfig {

  private int threshold = 70;
  private List<FraudDetectionRule> list = new ArrayList<>();

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public List<FraudDetectionRule> getList() {
    return list;
  }

  public void setList(List<FraudDetectionRule> list) {
    this.list = list;
  }
}
