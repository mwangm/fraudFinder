package com.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Individual rule trigger detail — each FraudRecord can have multiple triggered rules. */
@Entity
@Table(name = "fraud_record_details")
public class FraudRecordDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fraud_record_id", nullable = false)
  private FraudRecord result;

  @Column(nullable = false, length = 64)
  private String ruleName;

  @Column(nullable = false)
  private int score;

  @Column(length = 500)
  private String reason;

  protected FraudRecordDetail() {}

  public FraudRecordDetail(FraudRecord result, String ruleName, int score, String reason) {
    this.result = result;
    this.ruleName = ruleName;
    this.score = score;
    this.reason = reason;
  }

  public String getRuleName() {
    return ruleName;
  }

  public int getScore() {
    return score;
  }

  public String getReason() {
    return reason;
  }
}
