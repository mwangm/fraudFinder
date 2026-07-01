# 系统架构

## 架构图

```
┌──────────────────────────────────────────────────┐
│                  AWS EKS                          │
│                                                   │
│   SQS Queue ──▶ FraudDetectionService             │
│                   │                               │
│                   ├──▶ RuleEngine (SpEL)           │
│                   │      ├─ 大额交易 (40pts)       │
│                   │      ├─ 付款方黑名单 (80pts)    │
│                   │      └─ 收款方高风险 (60pts)    │
│                   │                               │
│                   ├──▶ FraudRecorderService        │
│                   │      └─ fraud_records (DB)     │
│                   │                               │
│                   └──▶ AlertService                │
│                          └─ SNS Topic ──▶ Email    │
│                                                   │
│   RiskCacheService (@Scheduled 60s)                │
│     ├─ suspicious_accounts ── 本地缓存              │
│     └─ payee_risks ── 本地缓存                     │
│                                                   │
└──────────────┬────────────────────────────────────┘
               │
               ▼
    ┌─────────────────────┐
    │    RDS MySQL         │
    │  ├─ fraud_records    │
    │  ├─ fraud_record_    │
    │  │  details          │
    │  ├─ suspicious_      │
    │  │  accounts         │
    │  └─ payee_risks      │
    └─────────────────────┘
```

## 数据流向

```
SQS Message { transactionId, accountId, payeeId, amount }
  │
  ▼
FraudDetectionService.onMessage()
  │  JSON 反序列化 → TransactionMessage
  │
  ▼
FraudDetectionService.detect()
  │  ├─ 幂等查: fraud_records.transaction_id
  │  ├─ RuleEngine.evaluate() → Optional<EvaluationResult>
  │  │    ├─ 总分 ≥ 70 → 欺诈
  │  │    └─ < 70 → 正常，不存 DB
  │  │
  │  ├─ [欺诈] FraudRecorderService.save()
  │  │    └─ fraud_records + fraud_record_details (级联)
  │  │
  │  └─ [欺诈] AlertService.publish()
  │       └─ SNS Topic → 邮件通知
```

## 包结构

```
com.fraudfinder/
├── model/       FraudRecord, FraudRecordDetail, SuspiciousAccount, PayeeRisk
├── repository/  FraudRecordRepository, SuspiciousAccountRepo, PayeeRiskRepo
├── message/     TransactionMessage, RuleEvaluationResult, EvaluationResult, AlertPayload
├── service/     FraudDetectionService, FraudRecorderService, AlertService, RiskCacheService
└── rule/        RuleEngine, FraudDetectionRule, RulesConfig
```

## 数据库

| 表 | 说明 |
|------|------|
| `fraud_records` | 欺诈记录（仅存 isFraud=true） |
| `fraud_record_details` | 每条规则明细（FK: fraud_record_id） |
| `suspicious_accounts` | 付款方黑名单 |
| `payee_risks` | 收款方风险等级 |

## 可靠性设计

- **幂等**: `fraud_records.transaction_id` 唯一约束，重复消息直接跳过
- **SQS 重试**: 可见性超时 + maxReceiveCount=3 → DLQ
- **优雅下线**: Spring Boot graceful shutdown + preStop sleep 15s
- **高可用**: 2 副本 + HPA + PDB + topologySpread

## 部署

- **平台**: AWS EKS (Helm Chart)
- **镜像**: Docker Hub `menyu168/fraud-detection`
- **CI/CD**: GitHub Actions (push main → check → build → push → deploy)
