# 开发任务计划

## 阶段 1：项目初始化
1. 初始化 Gradle 项目（Spring Boot 3.x，Java 21，Spotless + Google Java Format）
2. 配置依赖（RocketMQ、MySQL、Flyway、Caffeine、Actuator、JUnit、Testcontainers）
3. 搭建项目包结构（package-by-feature：transaction / detection / alert / risk / shared）

## 阶段 2：核心领域模型
4. 定义数据模型：Transaction、FraudDetectionResult、FraudDetectionRuleResult、SuspiciousAccount、PayeeRisk、Alert
5. 用 Flyway 管理数据库 Schema 迁移（版本化 SQL，部署可复现）
6. 实现 Repository 层（Spring Data JPA）
7. 关键索引：`transactions(transaction_id)` 唯一、`(account_id, timestamp)` 复合索引

## 阶段 3：规则引擎 & 检测服务
8. 设计 Rule 接口与 RuleResult，实现 6 条规则
9. HighFrequencyRule 用本地内存滑动窗口（ConcurrentHashMap + ConcurrentLinkedDeque）
10. 实现 FraudDetectionService 评分聚合逻辑

详见 [rule-engine.md](rule-engine.md)

## 阶段 4：消息 & API 接入 & 幂等
11. 实现 RocketMQ Consumer 接收交易消息
12. 实现 REST API（POST /transactions → 202 Accepted；GET 查询检测结果、告警；REST 路径异步化）
13. 实现幂等：transaction_id 唯一约束 + 查结果表短路（REST / MQ 双通道共用）
14. 实现 MQ 手动偏移量提交（检测 + DB 写入全部成功后提交，失败不提交走重试）
15. 实现告警通知（日志 + DB 持久化）

## 阶段 5：缓存 & 可观测性
16. Caffeine 本地缓存黑名单 + 收款方风险（TTL + 定时刷新）
17. 分布式日志：transactionId + traceId 进 MDC，MQ 跨边界传播，结构化 JSON 输出
18. Actuator health + metrics，预留 SLS 接入

## 阶段 6：测试
19. 单元测试（JUnit 5）：规则、评分、幂等、窗口计数器
20. 集成测试（Testcontainers：MySQL + RocketMQ）：MQ 消费、日志交互、端到端检测
21. 欺诈交易模拟测试（覆盖各规则触发 + 组合场景）
22. 覆盖率报告（目标：Line ≥ 85%，Branch ≥ 80%）

详见 [testing-strategy.md](testing-strategy.md)

## 阶段 7：韧性测试
23. 脚本化 `kubectl delete pod` 循环 + 持续流量，记录中断与恢复耗时
24. 可选 1~2 个 Chaos Mesh 实验（PodChaos + NetworkChaos）覆盖节点级故障
25. 韧性测试结果作为交付物

## 阶段 8：部署
26. 编写 Dockerfile
27. 编写 K8s 部署清单（Deployment、Service、HPA、PDB、topologySpread、Probe、优雅下线）
28. 编写 Helm Chart（可选）
29. 配置 CI/CD 流水线（GitHub Actions：6 阶段 + CD 手动触发，详见 [ci-cd.md](ci-cd.md)）

## 阶段 9：文档
30. 编写 README（部署 & 测试说明，一键运行）
31. API 契约已就绪 — 见 [api-contract.md](api-contract.md)
32. 测试策略已就绪 — 见 [testing-strategy.md](testing-strategy.md)
33. 架构图已就绪；设计选型 rationale 见 [tech-decisions.md](tech-decisions.md)
34. 整理韧性测试结果与覆盖率报告作为交付物
35. 后续设计决策见 [design-decisions-backlog.md](design-decisions-backlog.md)
