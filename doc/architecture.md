# System Architecture

## Architecture Diagram

```mermaid
graph LR
    CS[("Upstream<br/>Systems")] -->|"1. Submit"| SQS

    subgraph AWS[AWS Cloud]
        SQS[("Amazon SQS<br/>fraud-transactions")]
        SNS[("Amazon SNS<br/>fraud-detection-alerts")]
        RDS[("Amazon RDS<br/>MySQL 8.0")]

        subgraph EKS[Fraud Detection Service - EKS]
            RC[(RulesConfig<br/>application.yml)] -->|"Load"| RE
            TCS[TransactionConsumerService<br/>@SqsListener] -->|"3. Delegate"| FDS
            FDS[FraudDetectionService] -->|"4. Evaluate"| RE[RuleEngine<br/>∑ scores ≥ threshold]
            FDS -->|"5. Persist"| FR[FraudRecorderService]
            FDS -->|"6. Alert"| AL[AlertService]
        end
    end

    SQS -->|"2. Poll"| TCS
    RE -->|"Result"| FDS
    FR -->|"7. Save"| RDS
    AL -->|"8. Publish"| SNS
    SNS -->|"9. Notify"| AS[("Alert<br/>Subscribers")]
```

## Data Flow

```
SQS Message { transactionId, accountId, payeeId, amount }
  │
  ▼
TransactionConsumerService.onMessage()
  │ @Payload @Valid → TransactionMessage
  │
  ▼
FraudDetectionService.detect()
  │ Idempotency check (unique constraint on transaction_id)
  │ Insert Transaction record
  │
  ▼
RuleEngine.evaluate()
  │ Evaluates rules via SpEL expressions
  │ Total score ≥ threshold → DetectionResult
  │ Total score < threshold → discard
  │
  ├──────────────────────────┐
  ▼                          ▼
FraudRecorderService      AlertService.publish()
  │ Write to RDS             │ Format alert message
  ▼                          │ SNS notification
Amazon RDS MySQL 8.0         ▼
                          Alert Subscribers
```

## Technology Stack

| Component | Choice | Why |
|-----------|--------|-----|
| Language | Java 21 | Long-term support, Spring Boot ecosystem, strong typing for financial transactions |
| Framework | Spring Boot 3.4 | Mature DI, JPA, validation, Actuator — reduces boilerplate for REST + messaging apps |
| Rule Engine | SpEL (YAML-configured) | Spring built-in, zero extra dependencies, readable condition expressions without a DSL |
| Message Queue | Amazon SQS | Fully managed, no cluster overhead, native IAM + SNS integration |
| Database | Amazon RDS MySQL 8.0 | ACID unique constraints guarantee idempotency; relational model fits transaction–fraud record relationships |
| Compute | Amazon EKS | Always-warm JVM with custom tuning (ZGC); HPA auto-scaling by queue depth |
| Notifications | Amazon SNS | Managed pub/sub, email subscription, native SQS integration |
| Container Registry | Docker Hub | Simple, CI-friendly, no AWS-specific lock-in |
| Monitoring | Amazon CloudWatch | Unified AWS observability — logs (Fluent Bit), metrics, alarms |
| IaC | Terraform | Declarative, reproducible AWS infrastructure; modules for EKS, RDS, IAM |
| CI/CD | GitHub Actions + Helm | OIDC-based auth (no long-lived secrets), `helm upgrade --install` with `--wait` |
| Test Coverage | JaCoCo | 80% minimum enforced in CI, HTML + XML reports |
| Formatting | Spotless (google-java-format) | Consistent code style, enforced at CI check step |

