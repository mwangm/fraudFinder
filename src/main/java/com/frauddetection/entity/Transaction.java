package com.frauddetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Received transaction — serves as the idempotency guard via unique constraint on transaction_id.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @Column(name = "transaction_id", nullable = false, unique = true, length = 128)
  private String transactionId;

  @Column(name = "account_id", nullable = false, length = 64)
  private String accountId;

  @Column(name = "payee_id", nullable = false, length = 64)
  private String payeeId;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private Instant createdAt;

  protected Transaction() {}

  public Transaction(String transactionId, String accountId, String payeeId, BigDecimal amount) {
    this.transactionId = transactionId;
    this.accountId = accountId;
    this.payeeId = payeeId;
    this.amount = amount;
    this.createdAt = Instant.now();
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getPayeeId() {
    return payeeId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
