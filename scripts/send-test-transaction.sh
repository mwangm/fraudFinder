#!/bin/bash
set -e

QUEUE_URL="${SQS_QUEUE_URL:-https://sqs.ap-northeast-1.amazonaws.com/774075583755/fraud-transactions}"
REGION="${AWS_REGION:-ap-northeast-1}"

ACCOUNT_ID="${1:-ACC-TEST}"
PAYEE_ID="${2:-PE-1}"
AMOUNT="${3:-5000}"
TXN_ID="${4:-TXN-$(date +%s)}"

PAYLOAD=$(cat <<EOF
{
  "transactionId": "$TXN_ID",
  "accountId": "$ACCOUNT_ID",
  "payeeId": "$PAYEE_ID",
  "amount": $AMOUNT
}
EOF
)

echo "Sending: $PAYLOAD"

aws sqs send-message \
  --queue-url "$QUEUE_URL" \
  --message-body "$PAYLOAD" \
  --region "$REGION"

echo "Done. Transaction ID: $TXN_ID"
