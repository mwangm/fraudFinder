# Fraud Detection System

## Project Overview

Real-time fraud detection system that processes financial transactions from Amazon SQS. Uses a rule engine with SpEL-based rules to evaluate transactions and trigger alerts for suspicious activity.

## Key Features

- **SQS-driven ingestion**: Consumes transactions from Amazon SQS queue
- **Configurable rule engine**: YAML-defined rules with SpEL expressions, no code changes needed to add/modify rules
- **Real-time alerting**: Amazon SNS notifications for flagged transactions
- **High availability**: EKS multi-replica deployment with HPA auto-scaling
- **Infrastructure as Code**: Full Terraform-managed AWS infrastructure

## Technology Stack

### Application
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.1
- **Build Tool**: Gradle

### AWS Services
- **Compute**: Amazon EKS (Kubernetes 1.32)
- **Message Queue**: Amazon SQS (standard queue + DLQ)
- **Notification**: Amazon SNS
- **Container Registry**: Docker Hub (`menyu168/fraud-detection`)
- **Monitoring**: Amazon CloudWatch (logs, metrics, alarms)

### DevOps & Deployment
- **Infrastructure as Code**: Terraform
- **Container Orchestration**: Helm
- **CI/CD**: GitHub Actions (build → unit test -integration test → deploy pipeline)
- **Log and Metrics**:  CloudWatch
- **Test Coverage**: JaCoCo with 80% minimum threshold

## System Architecture

```
       ┌───────────────────────────────────────┐
       │  ┌──────────────────────────────┐     │
       │  │       Amazon SQS             │     │
       │  │  fraud-transactions          │     │
       │  └──────────────┬───────────────┘     │
       │                 │                     │
       │                 ▼                     │
       │  ┌──────────────────────────────┐     │
       │  │   Fraud Detection Service    │     │
       │  │   (EKS, 2-8 replicas)        │     │
       │  │   ┌──────────────────────┐   │     │
       │  │   │    Rule Engine        │   │     │
       │  │   │  YAML + SpEL rules    │   │     │
       │  │   │  Score >= threshold   │   │     │
       │  │   │  → trigger alert      │   │     │
       │  │   └──────────┬───────────┘   │     │
       │  └──────────────┼───────────────┘     │
       │                 │                     │
       │    ┌────────────▼──────────────┐      │
       │    │     Amazon SNS            │      │
       │    │  fraud-detection-alerts   │      │
       │    └────────────┬──────────────┘      │
       │                 │                     │
       │    ┌────────────▼──────────────┐      │
       │    │   Email / Notification    │      │
       │    └───────────────────────────┘      │
       │                                       │
       └───────────────────────────────────────┘
```

## Directory Structure

### `infra/terraform/`
Terraform configurations for AWS infrastructure.

### `charts/fraud-detection/`
Helm chart for deploying the application on Kubernetes.

### `src/`
Spring Boot application source code.

### `doc/`
Project documentation
- System architecture doc
- AWS configurations 
- Alert notification simples

### `.github/workflows/`
GitHub Actions workflow definitions for:
- Continuous Integration (spotless, test, coverage gate)
- Build artifacts
- Deployment to EKS

### `scripts/`
- `send-test-transaction.sh` — Send test SQS messages (fraud/normal/custom scenarios, `--help` for details)

## Testing

## Getting Started

### Prerequisites

- AWS Account with appropriate permissions
- Java 21
- Docker Desktop
- kubectl & Helm 3.x
- Terraform 1.5+

### Local Development

```bash
# Build the application
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage verification
./gradlew check

# Run integration tests，make sure docker running as the precondition
```


### Infrastructure Deployment

```bash
cd infra/terraform

# Initialize Terraform
terraform init

# Review and apply
terraform plan
terraform apply
```

### Application Deployment

# Build and Deploy using GitHub Actions pipeline by Helm

# Send test transaction
```bash
# Fraud scenario (triggers alert):
./scripts/send-test-transaction.sh fraud

# Normal scenario (no alert):
./scripts/send-test-transaction.sh normal

# See all options:
./scripts/send-test-transaction.sh help
```
