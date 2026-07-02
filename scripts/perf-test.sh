#!/bin/bash
set -e

QUEUE_URL="${SQS_QUEUE_URL:-https://sqs.ap-northeast-1.amazonaws.com/774075583755/fraud-transactions}"
REGION="${AWS_REGION:-ap-northeast-1}"
TOTAL="${TOTAL:-100}"
CONCURRENCY="${CONCURRENCY:-5}"
FRAUD_RATIO="${FRAUD_RATIO:-30}"  # % of messages that are fraudulent

usage() {
  echo "Usage: $0 [options]"
  echo ""
  echo "Performance test — sends batches of SQS messages and measures throughput."
  echo ""
  echo "Options:"
  echo "  -n N     Number of messages (default: 100)"
  echo "  -c N     Concurrent senders (default: 5)"
  echo "  -f N     Fraud percentage 0-100 (default: 30)"
  echo ""
  echo "Env vars:"
  echo "  SQS_QUEUE_URL   SQS queue URL"
  echo "  AWS_REGION      AWS region (default: ap-northeast-1)"
  echo ""
  echo "Examples:"
  echo "  $0 -n 500 -c 10           # 500 messages, 10 concurrent senders"
  echo "  $0 -n 1000 -f 50           # 1000 messages, 50% fraud"
  echo "  $0 -n 5000 -c 20 -f 0      # 5000 normal-only messages"
  exit 1
}

# --- Parse args ---
while getopts "n:c:f:h" opt; do
  case $opt in
    n) TOTAL="$OPTARG" ;;
    c) CONCURRENCY="$OPTARG" ;;
    f) FRAUD_RATIO="$OPTARG" ;;
    *) usage ;;
  esac
done

# --- Pre-flight ---
if ! command -v aws &>/dev/null; then
  echo "ERROR: aws CLI not found. Install: brew install awscli"
  exit 1
fi

echo "=========================================="
echo "  Fraud Detection — Performance Test"
echo "=========================================="
echo "  Queue:       $QUEUE_URL"
echo "  Messages:    $TOTAL"
echo "  Concurrency: $CONCURRENCY senders"
echo "  Fraud ratio: $FRAUD_RATIO%"
echo "  Region:      $REGION"
echo "=========================================="
echo ""

# --- Generate messages ---
TMPDIR=$(mktemp -d)
trap "rm -rf $TMPDIR" EXIT

generate_payload() {
  local i=$1
  local txn_id="PTX-$(printf "%06d" $i)-$(date +%s)"
  if [ $((i % 100)) -lt "$FRAUD_RATIO" ]; then
    # Fraudulent: blacklisted account triggers suspicious-payer-account rule
    echo "{\"transactionId\":\"$txn_id\",\"accountId\":\"ACC-BAD\",\"payeeId\":\"PE-1\",\"amount\":5000}"
  else
    # Normal
    echo "{\"transactionId\":\"$txn_id\",\"accountId\":\"NORMAL-ACC\",\"payeeId\":\"PE-1\",\"amount\":500}"
  fi
}

# Pre-generate all payloads (avoids subshell overhead during send)
for ((i=1; i<=TOTAL; i++)); do
  generate_payload "$i" > "$TMPDIR/msg-$i.json"
done

# --- Send function for one worker ---
send_batch() {
  local worker_id=$1
  local start=$2
  local end=$3
  local start_time
  local elapsed
  start_time=$(date +%s%N)

  for ((i=start; i<=end; i++)); do
    aws sqs send-message \
      --queue-url "$QUEUE_URL" \
      --message-body "file://$TMPDIR/msg-$i.json" \
      --region "$REGION" \
      --no-cli-pager >/dev/null 2>&1
  done

  elapsed=$((($(date +%s%N) - start_time) / 1000000))
  echo "$worker_id:$elapsed:$((end - start + 1))" > "$TMPDIR/result-$worker_id"
}

# --- Run workers in parallel ---
BATCH_SIZE=$((TOTAL / CONCURRENCY))
REMAINDER=$((TOTAL % CONCURRENCY))

START_TIME=$(date +%s%N)

current=1
for ((w=0; w<CONCURRENCY; w++)); do
  end=$((current + BATCH_SIZE - 1))
  if [ $w -lt $REMAINDER ]; then
    end=$((end + 1))
  fi
  send_batch "$w" "$current" "$end" &
  pids[$w]=$!
  current=$((end + 1))
done

# --- Wait & collect results ---
for pid in "${pids[@]}"; do
  wait "$pid"
done

END_TIME=$(date +%s%N)
TOTAL_MS=$((($END_TIME - START_TIME) / 1000000))

# --- Report ---
echo ""
echo "=========================================="
echo "  Results"
echo "=========================================="

for ((w=0; w<CONCURRENCY; w++)); do
  read -r wid elapsed count < "$TMPDIR/result-$w"
  rate=$(echo "scale=1; $count * 1000 / $elapsed" | bc 2>/dev/null || echo "0")
  printf "  Worker %2s:  %4d msgs in %6d ms  (%6.1f msg/s)\n" "$wid" "$count" "$elapsed" "$rate"
done

TPS=$(echo "scale=1; $TOTAL * 1000 / $TOTAL_MS" | bc 2>/dev/null || echo "0")
echo "  ----------------------------------------"
echo "  Total:     $TOTAL messages in ${TOTAL_MS}ms"
echo "  Throughput: ${TPS} msg/s"
echo "=========================================="

echo ""
echo "Check results:"
echo "  # Query recent test transactions"
echo "  aws sqs receive-message --queue-url $QUEUE_URL --max-number-of-messages 1 --region $REGION"

if [ $TOTAL_MS -gt 0 ]; then
  echo ""
  echo "[PASS] Test completed successfully."
fi
