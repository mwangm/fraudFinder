# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Real-time fraud detection system — analyzes financial transactions in real-time using rule-based detection, deployed on AWS EKS.

## Documentation

- **[需求文档](doc/assignment.md)** — 作业原始需求
- **[技术选型](doc/tech-decisions.md)** — 技术栈选型决策
- **[系统架构](doc/architecture.md)** — 架构设计、数据流、模块分层
- **[规则引擎](doc/rule-engine.md)** — 策略模式设计、6 条规则实现细节、扩展指南
- **[API 契约](doc/api-contract.md)** — REST API 端点、请求/响应格式、SQS 消息契约
- **[测试策略](doc/testing-strategy.md)** — 测试分层、单元/集成/欺诈模拟/韧性测试
- **[CI/CD 流水线](doc/ci-cd.md)** — 6 阶段流水线 + CD + Nightly
- **[任务计划](doc/task-plan.md)** — 开发任务步骤
- **[设计决策待办](doc/design-decisions-backlog.md)** — 待细化的架构决策

## Tech Stack

Java 21, Spring Boot 3.x, Gradle (单模块), Amazon SQS, RDS MySQL, EKS, CloudWatch.
详见 [doc/tech-decisions.md](doc/tech-decisions.md).

## Architecture

- **双通道接入**：REST API + SQS Listener，两种方式接收交易请求
- **规则引擎**：策略模式 + sealed interface，6 条规则，Spring Bean 自动注册。详见 [doc/rule-engine.md](doc/rule-engine.md)
- **部署**：EKS + Helm + Deployment/Service/HPA，高可用

## Infrastructure

- **容器镜像**：Docker Hub (`menyu168/fraud-detection`)
- **编排**：AWS EKS (Kubernetes) + Helm Chart (`charts/fraud-detection/`)
- **消息队列**：Amazon SQS (标准队列 + DLQ，可见性超时替代 offset 提交)
- **数据库**：Amazon RDS MySQL 8.0
- **日志**：Amazon CloudWatch Logs
- **IaC**：Terraform (`infra/terraform/`) — ECR, SQS, RDS, EKS, IAM

## Key Migration Notes

- RocketMQ `@RocketMQMessageListener` → SQS `@SqsListener` (Spring Cloud AWS 3.3.0)
- RocketMQ offset commit → SQS `deletionPolicy = ON_SUCCESS` + visibility timeout
- Message retry: RocketMQ redelivery → SQS redrive policy (maxReceiveCount=3 → DLQ)
- Idempotency unchanged: DB unique constraint on `detection_results.transaction_id`
- `TransactionService.acceptAndDetectSync()` unchanged — only consumer wrapper changed
