# 系统架构

## 架构图

```mermaid
graph LR
    CS[("Upstream<br/>Systems")] -->|"1. Submit"| SQS

    subgraph AWS[AWS Cloud]
        SQS[("Amazon SQS<br/>fraud-transactions")]
        SNS[("Amazon SNS<br/>fraud-detection-alerts")]

        subgraph EKS[Fraud Detection Service - EKS]
            RC[(RulesConfig<br/>application.yml)] -->|"Load"| RE
            FDS[FraudDetectionService<br/>@SqsListener] -->|"3. Evaluate"| RE[RuleEngine<br/>∑ scores ≥ threshold]
            FDS -->|"5. Alert"| AL[AlertService<br/>publish to SNS]
        end
    end

    SQS -->|"2. Poll"| FDS
    RE -->|"4. Result"| FDS
    AL -->|"6. Publish"| SNS
    SNS -->|"7. Notify"| AS[("Alert<br/>Subscribers")]
```

## 数据流向

```
SQS Message { transactionId, accountId, payeeId, amount }
  │
  ▼
FraudDetectionService.onMessage()
  │ JSON → TransactionMessage
  │
  ▼
RuleEngine.evaluate()
  │ SpEL 表达式评估 3 条规则
  │ 总分 ≥ threshold → DetectionResult
  │ 总分 < threshold → 丢弃
  │
  ▼
AlertService.publish()
  │ 格式化告警文本
  │ SNS Topic → 邮件通知
```
