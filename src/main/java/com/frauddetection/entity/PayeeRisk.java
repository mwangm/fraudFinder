package com.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Payee risk level — reference data loaded into RiskCacheService. */
@Entity
@Table(name = "payee_risks")
public class PayeeRisk {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String payeeId;

  @Column(nullable = false, length = 20)
  private String riskLevel;

  @Column(length = 500)
  private String reason;

  @Column(nullable = false)
  private Instant updatedAt;

  protected PayeeRisk() {}

  public PayeeRisk(String payeeId, String riskLevel, String reason, Instant updatedAt) {
    this.payeeId = payeeId;
    this.riskLevel = riskLevel;
    this.reason = reason;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getPayeeId() {
    return payeeId;
  }

  public String getRiskLevel() {
    return riskLevel;
  }

  public String getReason() {
    return reason;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
