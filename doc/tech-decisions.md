# 技术选型

| 选型 | 决定 | 理由 |
|------|------|------|
| 语言 | Java 21 | LTS，record 适合领域建模 |
| 框架 | Spring Boot 3.4 | Actuator/K8s/可观测性集成完善 |
| 构建 | Gradle (Kotlin DSL) | 增量构建快 |
| 云平台 | AWS | EKS/SQS/SNS 一套闭环 |
| 容器编排 | EKS | 托管 K8s + Helm 部署 |
| 消息队列 | Amazon SQS | 标准队列 + DLQ |
| 告警 | Amazon SNS | Email 订阅 |
| 规则引擎 | SpEL（YAML 配置化） | 改规则只改 YAML，不写 Java，不依赖数据库 |
| 镜像仓库 | Docker Hub | 公开拉取 |
| CI/CD | GitHub Actions | push main → check → build → push → deploy |
| 代码覆盖率 | JaCoCo | 测试覆盖率报告 |
