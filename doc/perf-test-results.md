# Performance Test Results

**Date**: 2026-07-02

**Environment**: EKS 2 pods (250m CPU / 512Mi each), RDS MySQL 8.0 (db.t3.micro), SQS standard queue

**Script**: `scripts/perf-test.sh`

---

## Test 1: Baseline — 100 messages, 5 concurrent senders, 30% fraud

```bash
scripts/perf-test.sh -n 100 -c 5 -f 30
```

| Metric | Value |
|--------|-------|
| Total messages | 100 |
| Fraud messages | 30 |
| Senders | 5 |
| Total time | 12,350 ms |
| Throughput | 8.1 msg/s |
| Worker 0 | 20 msgs in 11,230 ms (1.8 msg/s) |
| Worker 1 | 20 msgs in 12,140 ms (1.6 msg/s) |
| Worker 2 | 20 msgs in 12,350 ms (1.6 msg/s) |
| Worker 3 | 20 msgs in 11,890 ms (1.7 msg/s) |
| Worker 4 | 20 msgs in 12,080 ms (1.7 msg/s) |

---

## Test 2: Medium Load — 500 messages, 10 concurrent senders, 30% fraud

```bash
scripts/perf-test.sh -n 500 -c 10 -f 30
```

| Metric | Value |
|--------|-------|
| Total messages | 500 |
| Fraud messages | 150 |
| Senders | 10 |
| Total time | 28,710 ms |
| Throughput | 17.4 msg/s |

---

## Test 3: High Load — 1,000 messages, 20 concurrent senders, 50% fraud

```bash
scripts/perf-test.sh -n 1000 -c 20 -f 50
```

| Metric | Value |
|--------|-------|
| Total messages | 1,000 |
| Fraud messages | 500 |
| Senders | 20 |
| Total time | 53,420 ms |
| Throughput | 18.7 msg/s |

---

## Test 4: Sustained Load — 5,000 messages, 20 concurrent senders, 30% fraud

```bash
scripts/perf-test.sh -n 5000 -c 20 -f 30
```

| Metric | Value |
|--------|-------|
| Total messages | 5,000 |
| Fraud messages | 1,500 |
| Senders | 20 |
| Total time | 261,800 ms (~4.4 min) |
| Throughput | 19.1 msg/s |

---

## Summary

| Test | Messages | Throughput | Notes |
|------|----------|------------|-------|
| Baseline | 100 | 8.1 msg/s | Warm-up, low concurrency |
| Medium | 500 | 17.4 msg/s | Concurrency scaling |
| High | 1,000 | 18.7 msg/s | 50% fraud, rule engine taxed |
| Sustained | 5,000 | 19.1 msg/s | Steady state, stable throughput |

### Observations

- **Throughput plateaus at ~19 msg/s** — limited by `aws sqs send-message` CLI latency (single HTTP call per message, ~50ms RTT)
- **Fraud ratio has minimal impact on send throughput** — message body size is constant
- **Concurrency scales linearly up to ~10 senders**, diminishing returns beyond that due to SQS API rate limits
- **Sustained test ran 4.4 minutes with no errors** — connection stability confirmed

### Recommendations

- Use `send-message-batch` (10 messages per API call) for production load generation to achieve ~100+ msg/s
- For real throughput measurement, monitor EKS pod metrics and SQS queue depth rather than sender-side timing
