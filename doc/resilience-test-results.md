# Resilience Test Results

## Test 1: Pod Kill Recovery

**Date**: 2026-07-02

**Scenario**: Kill a running pod and verify system recovers without data loss.

**Steps**:
1. Send pre-kill transaction to SQS
2. Delete a fraud-detection pod
3. Wait for new pod to become ready
4. Send post-kill transaction
5. Verify both messages processed

**Results**:

| Metric | Value |
|--------|-------|
| Pod recovery time | < 120s |
| Messages lost | 0 |
| Pre-kill message processed | PASS |
| Post-kill message processed | PASS |

---

## Test 2: Node Failure Recovery

**Date**: 2026-07-02

**Scenario**: Both EKS worker nodes became NotReady (kubelet stopped) due to memory pressure caused by leftover OTel auto-instrumentation init containers.

**Timeline**:

| Time | Event |
|------|-------|
| T+0 | Node `ip-172-31-15-83` and `ip-172-31-46-127` go NotReady |
| T+5m | Identified root cause: stale mutating webhook injecting OTel init containers |
| T+7m | Deleted mutating webhook, all pods force deleted |
| T+9m | Terminated unhealthy EC2 instances |
| T+12m | ASG launched replacement nodes |
| T+15m | New nodes joined cluster as Ready |
| T+16m | Fraud detection pods restarted, health checks passed |
| T+17m | SQS message processing verified |

**Results**:

| Metric | Value |
|--------|-------|
| Total downtime | ~15 minutes |
| SQS messages lost | 0 (messages retained in queue, processed after recovery) |
| Root cause | Leftover `amazon-cloudwatch-observability` mutating webhook |
| Fix applied | Deleted webhook, reduced HPA max replicas 8→4, removed memory HPA metric |

**Lessons Learned**:
- EKS add-on deletion does not clean up mutating webhooks — manual cleanup required
- Memory-based HPA scaling can cause cascading node failures
- SQS visibility timeout + DLQ ensures zero message loss during extended outages
- ASG auto-recovery works as designed for node-level failures
