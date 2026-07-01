package com.frauddetection.model;

import jakarta.persistence.*;
import java.time.Instant;

/** 收款方风险等级 — 参考数据，通过 RiskCacheService 每 60 秒从 DB 全量刷新到本地缓存。 */
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
