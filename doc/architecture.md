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
  │ 总分 ≥ 70 → DetectionResult
  │ 总分 < 70 → 丢弃
  │
  ▼
AlertService.publish()
  │ 格式化告警文本
  │ SNS Topic → 邮件通知
```

## 包结构

```
com.frauddetection/
├── model/       TransactionMessage, DetectionResult, DetectionResultDetail
├── rule/        RuleEngine, FraudDetectionRule, RulesConfig
└── service/     FraudDetectionService, AlertService
```

## 规则引擎

3 条 SpEL 规则，YAML 可配置，无需数据库：

```yaml
fraud:
  rules:
    threshold: 70
    list:
      - name: "amount-threshold"
        condition: "amount > 100000"
        score: 40
      - name: "suspicious-account"
        condition: "{'ACC-BAD','ACC-FRAUD','ACC-SCAM'}.contains(accountId)"
        score: 80
      - name: "high-risk-payee"
        condition: "{'PE-HIGH','PE-RISK'}.contains(payeeId)"
        score: 60
```

## 可靠性设计

- **SQS 重试**: 可见性超时 + maxReceiveCount=3 → DLQ
- **错误处理**: JSON 解析失败直接 ACK，业务异常传播触发重试
- **优雅下线**: Spring Boot graceful shutdown + preStop sleep 15s
- **高可用**: 2 副本 + HPA + PDB + topologySpread

## 部署

- **平台**: AWS EKS (Helm Chart)
- **镜像**: Docker Hub `menyu168/fraud-detection`
- **CI/CD**: GitHub Actions (push main → check → build → push → deploy)
