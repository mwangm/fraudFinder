# Rule Engine

## Design

SpEL (Spring Expression Language) driven — rules are defined in YAML, no database or Java code changes needed.

## Rule Definition Example

```yaml
fraud:
  rules:
    threshold: 70
    list:
      - name: "amount-threshold"
        condition: "amount > 100000"
        score: 40
        alertMessage: "Large amount transaction detected"
        enabled: true
      - name: "suspicious-payer-account"
        condition: "#riskCache.isSuspiciousAccount(accountId)"
        score: 80
        alertMessage: "Payer account is blacklisted"
        enabled: true
      - name: "high-risk-payee"
        condition: "#riskCache.isHighRiskPayee(payeeId)"
        score: 60
        alertMessage: "Payee is high risk"
        enabled: true
```

## Available Variables

All `TransactionMessage` fields are accessible in SpEL expressions:

| Variable | Type | Description |
|----------|------|-------------|
| `transactionId` | String | Unique transaction identifier |
| `accountId` | String | Payer account ID |
| `payeeId` | String | Payee ID |
| `amount` | BigDecimal | Transaction amount |
| `#riskCache` | RiskCacheService | Risk data cache (isSuspiciousAccount, isHighRiskPayee) |

## Scoring & Judgment

| Scenario | Triggered Rules | Total Score | Verdict |
|----------|----------------|-------------|---------|
| Large transfer | amount-threshold | 40 | Normal |
| Blacklisted payer | suspicious-payer-account | 80 | Fraud |
| High-risk payee | high-risk-payee | 60 | Normal |
| Large + high-risk payee | amount-threshold + high-risk-payee | 100 | Fraud |
| All rules triggered | All rules | 180 | Fraud |

## Extension Guide

**Add a rule**: Add a new entry to the YAML list and restart the service.
**Disable a rule**: Set `enabled: false`.
**Adjust threshold**: Modify `fraud.rules.threshold`.
