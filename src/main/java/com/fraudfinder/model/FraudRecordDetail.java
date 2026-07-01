package com.fraudfinder.model;

import jakarta.persistence.*;

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

  public String getRuleName() { return ruleName; }
  public boolean isTriggered() { return true; }
  public int getScore() { return score; }
  public String getReason() { return reason; }
}
