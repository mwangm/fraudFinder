-- V1: Schema for fraud detection system

CREATE TABLE fraud_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    total_score INT NOT NULL,
    threshold INT NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_fr_transaction_id UNIQUE (transaction_id)
);

CREATE TABLE fraud_record_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fraud_record_id BIGINT NOT NULL,
    rule_name VARCHAR(64) NOT NULL,
    score INT NOT NULL,
    reason VARCHAR(500)
);
CREATE INDEX idx_frd_record_id ON fraud_record_details(fraud_record_id);

-- 付款方黑名单
CREATE TABLE suspicious_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_sa_account_id UNIQUE (account_id)
);

-- 收款方风险等级
CREATE TABLE payee_risks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payee_id VARCHAR(64) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_pr_payee_id UNIQUE (payee_id)
);
