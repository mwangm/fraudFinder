package com.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Blacklisted payer account — reference data loaded into RiskCacheService. */
@Entity
@Table(name = "suspicious_accounts")
public class SuspiciousAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String accountId;

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(nullable = false)
  private Instant createdAt;

  protected SuspiciousAccount() {}

  public SuspiciousAccount(String accountId, String reason, Instant createdAt) {
    this.accountId = accountId;
    this.reason = reason;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
