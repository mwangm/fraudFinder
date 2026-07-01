package com.fraudfinder.rule;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fraud.rules")
public class RulesConfig {
  private static final Logger log = LoggerFactory.getLogger(RulesConfig.class);

  private List<FraudDetectionRule> list = new ArrayList<>();

  public List<FraudDetectionRule> getList() {
    return list;
  }

  public void setList(List<FraudDetectionRule> list) {
    this.list = list;
  }
}
