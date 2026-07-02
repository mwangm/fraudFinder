# 规则引擎

## 设计

SpEL（Spring Expression Language）表达式驱动，规则定义在 YAML 中，无需数据库、无需编写 Java 代码。

## 规则定义示例

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

## 可用变量

SpEL 表达式中可直接访问 `TransactionMessage` 的所有字段：

| 变量 | 类型 | 说明 |
|------|------|------|
| `transactionId` | String | 交易唯一标识 |
| `accountId` | String | 付款方账户 |
| `payeeId` | String | 收款方 |
| `amount` | BigDecimal | 交易金额 |
| `#riskCache` | RiskCacheService | 风险数据缓存 (isSuspiciousAccount, isHighRiskPayee) |

## 评分与判定

| 场景 | 触发规则 | 总分 | 判定 |
|------|---------|------|------|
| 大额转账 | amount-threshold | 40 | 正常 |
| 付款方黑名单 | suspicious-payer-account | 80 | 欺诈 |
| 高风险收款方 | high-risk-payee | 60 | 正常 |
| 大额 + 高风险收款方 | amount-threshold + high-risk-payee | 100 | 欺诈 |
| 全触发 | 全部规则 | 180 | 欺诈 |

## 扩展指南

**新增规则**: 在 YAML 中加一条配置，重启生效。
**禁用规则**: 设置 `enabled: false`。
**调阈值**: 修改 `fraud.rules.threshold`。
