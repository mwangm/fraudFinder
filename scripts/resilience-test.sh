#!/bin/bash
# Resilience Test: Pod Kill & Recovery
# Verifies that the fraud detection service recovers from pod failures
# without data loss.

set -e

NAMESPACE="fraud-detection"
QUEUE_URL="https://sqs.ap-northeast-1.amazonaws.com/774075583755/fraud-transactions"
TXN_ID_PREFIX="resilience-$(date +%s)"

echo "=== Resilience Test Started ==="
echo ""

# 1. Record initial pod list
echo "[1/6] Recording initial pod state..."
INITIAL_PODS=$(kubectl get pods -n ${NAMESPACE} -o name)
echo "${INITIAL_PODS}"
echo ""

# 2. Send test transaction before kill
echo "[2/6] Sending pre-kill transaction..."
TXN_1="${TXN_ID_PREFIX}-pre"
aws sqs send-message \
  --queue-url ${QUEUE_URL} \
  --message-body "{\"transactionId\":\"${TXN_1}\",\"amount\":500,\"accountId\":\"NORMAL-ACC\",\"payeeId\":\"PE-1\"}" \
  --region ap-northeast-1 > /dev/null
echo "Sent: ${TXN_1}"
echo ""

# 3. Kill a running pod
echo "[3/6] Killing a pod..."
TARGET_POD=$(kubectl get pods -n ${NAMESPACE} -o name | head -1)
echo "Killing: ${TARGET_POD}"
kubectl delete ${TARGET_POD} -n ${NAMESPACE} --grace-period=5
echo ""

# 4. Wait for new pod to be ready
echo "[4/6] Waiting for new pod to be ready..."
kubectl wait --for=condition=ready pod -l app=fraud-detection \
  -n ${NAMESPACE} --timeout=180s
echo "Pod ready!"
echo ""

# 5. Verify all replicas running
echo "[5/6] Checking deployment health..."
kubectl rollout status deploy fraud-detection -n ${NAMESPACE} --timeout=120s
echo ""

# 6. Send post-kill transaction
echo "[6/6] Sending post-kill transaction..."
TXN_2="${TXN_ID_PREFIX}-post"
aws sqs send-message \
  --queue-url ${QUEUE_URL} \
  --message-body "{\"transactionId\":\"${TXN_2}\",\"amount\":500,\"accountId\":\"NORMAL-ACC\",\"payeeId\":\"PE-1\"}" \
  --region ap-northeast-1 > /dev/null
echo "Sent: ${TXN_2}"
echo ""

# 7. Verify both messages processed
echo "=== Verifying message processing ==="
sleep 10
POD_NAME=$(kubectl get pods -n ${NAMESPACE} -o name | head -1)
LOGS=$(kubectl logs -n ${NAMESPACE} ${POD_NAME} --tail=50)

if echo "${LOGS}" | grep -q "${TXN_1}"; then
  echo "PASS: Pre-kill message (${TXN_1}) processed"
else
  echo "FAIL: Pre-kill message (${TXN_1}) not found in logs"
  exit 1
fi

if echo "${LOGS}" | grep -q "${TXN_2}"; then
  echo "PASS: Post-kill message (${TXN_2}) processed"
else
  echo "FAIL: Post-kill message (${TXN_2}) not found in logs"
  exit 1
fi

echo ""
echo "=== Resilience Test PASSED ==="
echo "Pod recovery time: < 3 minutes"
echo "Messages lost: 0"
