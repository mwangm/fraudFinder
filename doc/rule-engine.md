# 规则引擎

## 设计

SpEL（Spring Expression Language）表达式驱动，规则定义在 YAML 中，无需编写 Java 代码即可新增/修改规则。

## 规则定义

```yaml
fraud:
  detection:
    threshold: 70
  rules:
    list:
      - name: "大额交易"
        condition: "amount > 100000"
        score: 40
        priority: 1
        enabled: true
      - name: "付款方黑名单"
        condition: "#riskCache.isSuspicious(accountId)"
        score: 80
        priority: 2
        enabled: true
      - name: "收款方高风险"
        condition: "#riskCache.getPayeeRisk(payeeId)?.riskLevel == 'HIGH'"
        score: 60
        priority: 3
        enabled: true
```

## 规则评估流程

```
TransactionMessage → RuleEngine.evaluate()
  │
  ├─ 过滤 enabled = true 的规则
  ├─ 逐条解析 SpEL 表达式，注入 riskCache 变量
  ├─ 汇总分数
  │
  ├─ totalScore ≥ threshold → Optional.of(EvaluationResult)
  └─ totalScore < threshold → Optional.empty()
```

## 评分与判定

| 场景 | 触发规则 | 总分 | 判定 |
|------|---------|------|------|
| 大额转账 | 规则1 | 40 | 正常 |
| 付款方黑名单 | 规则2 | 80 | 欺诈 |
| 高风险收款方 | 规则3 | 60 | 正常 |
| 大额 + 高风险收款方 | 规则1+3 | 100 | 欺诈 |
| 全触发 | 规则1+2+3 | 180 | 欺诈 CRITICAL |

## 扩展指南

**新增规则**:
```yaml
- name: "新规则"
  condition: "amount > 5000"       # SpEL 表达式
  score: 30
  priority: 4
  enabled: true
```

**禁用规则**: `enabled: false`

**调整阈值**: `fraud.detection.threshold: 60`

**SpEL 可用变量**:
- TransactionMessage 字段: `transactionId`, `accountId`, `payeeId`, `amount`
- `#riskCache`: RiskCacheService（isSuspicious, getPayeeRisk）
