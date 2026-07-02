# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Real-time fraud detection system — analyzes financial transactions in real-time using rule-based detection, deployed on AWS EKS.

## Documentation

- **[需求文档](doc/assignment.md)** — 作业原始需求
- **[系统架构](doc/architecture.md)** — 架构设计、数据流、模块分层
- **[规则引擎](doc/rule-engine.md)** — 策略模式设计、规则实现细节、扩展指南


## Project Structure
```
fraudFinder/
├── .github/workflows/       # CI/CD pipelines
├── charts/fraud-detection/  # Helm chart (EKS deployment)
├── doc/                     # Architecture & design docs
├── infra/terraform/         # IaC (EKS, RDS, SQS, SNS, IAM)
├── scripts/                 # Utility scripts (deploy, test, seed data)
├── src/main/java/.../
│   ├── controller/          # REST API endpoints
│   ├── entity/              # JPA entities
│   ├── model/               # DTOs / value objects
│   ├── repository/          # Spring Data JPA repositories
│   ├── rule/                # Rule engine (strategy pattern)
│   └── service/             # Business logic layer
├── src/main/resources/
│   └── db/migration/        # Flyway migrations
├── src/test/java/.../
├── Dockerfile
├── build.gradle.kts
└── README.md
```

