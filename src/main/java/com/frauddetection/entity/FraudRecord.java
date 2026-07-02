package com.frauddetection.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Fraud detection result — persisted when total score ≥ threshold. 1:1 with Transaction. */
@Entity
@Table(name = "fraud_records")
public class FraudRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String transactionId;

  @Column(nullable = false)
  private int totalScore;

  @Column(nullable = false)
  private int threshold;

  @Column(nullable = false)
  private Instant detectedAt;

  @OneToMany(mappedBy = "result", cascade = CascadeType.ALL)
  private List<FraudRecordDetail> details = new ArrayList<>();

  protected FraudRecord() {}

  public FraudRecord(String transactionId, int totalScore, int threshold, Instant detectedAt) {
    this.transactionId = transactionId;
    this.totalScore = totalScore;
    this.threshold = threshold;
    this.detectedAt = detectedAt;
  }

  public Long getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public int getThreshold() {
    return threshold;
  }

  public Instant getDetectedAt() {
    return detectedAt;
  }

  public List<FraudRecordDetail> getDetails() {
    return details;
  }

  public void addDetail(FraudRecordDetail detail) {
    details.add(detail);
  }
}
