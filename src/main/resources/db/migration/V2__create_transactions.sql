CREATE TABLE transactions (
    transaction_id VARCHAR(128) NOT NULL PRIMARY KEY,
    account_id     VARCHAR(64)  NOT NULL,
    payee_id       VARCHAR(64)  NOT NULL,
    amount         DECIMAL(18,2) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
