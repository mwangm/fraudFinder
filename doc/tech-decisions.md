# 技术选型

| 选型 | 决定 | 理由 |
|------|------|------|
| 语言 | Java 21 | LTS，record/sealed 适合领域建模 |
| 框架 | Spring Boot 3.4 | Actuator/K8s/可观测性集成完善 |
| 构建 | Gradle (Kotlin DSL) | 增量构建快 |
| 云平台 | AWS | EKS/SQS/SNS/RDS/CloudWatch 一套闭环 |
| 容器编排 | EKS | 托管 K8s + Helm 部署 |
| 消息队列 | Amazon SQS | 标准队列 + DLQ，可见性超时替代 offset 提交 |
| 告警通知 | Amazon SNS | 发布/订阅，Email/Lambda/HTTP 灵活 |
| 数据库 | Amazon RDS MySQL 8.0 | 兼容 MySQL，Flyway 迁移 |
| 规则引擎 | SpEL（YAML 配置化） | 规则改 YAML 不写 Java，比 Drools 轻量 |
| 缓存 | @Scheduled 定时刷新 | 黑名单/收款方风险量小，ConcurrentHashMap 本地缓存 |
| 幂等 | DB 唯一约束 (transaction_id) | 金融场景正确性优先 |
| 迁移 | Flyway | 版本化 SQL |
| 镜像仓库 | Docker Hub | 公开拉取，简单 |
| CI/CD | GitHub Actions | push main → check → build → push → deploy |
