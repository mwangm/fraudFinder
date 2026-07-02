#!/bin/bash
set -e

QUEUE_URL="${SQS_QUEUE_URL:-https://sqs.ap-northeast-1.amazonaws.com/774075583755/fraud-transactions}"
REGION="${AWS_REGION:-ap-northeast-1}"

usage() {
  echo "Usage: $0 <fraud|normal|help|custom> [args...]"
  echo ""
  echo "Scenarios:"
  echo "  $0 fraud [amount]     - Triggers alert (blacklisted account ACC-BAD, score 80 ≥ threshold 70)"
  echo "  $0 normal [amount]    - Clean transaction (NORMAL-ACC, score 0, no alert)"
  echo "  $0 custom <acc> <payee> <amount>"
  echo "                        - Custom transaction"
  echo "  $0 help               - Show this help"
  echo ""
  echo "Rules (from application.yml):"
  echo "  amount-threshold        amount > 100000 → score 40"
  echo "  suspicious-payer-account accountId in {ACC-BAD, ACC-FRAUD, ACC-SCAM} → score 80"
  echo "  high-risk-payee         payeeId in {PE-HIGH, PE-RISK} → score 60"
  echo "  Threshold: 70 (total score ≥ 70 triggers alert)"
  echo ""
  echo "Examples:"
  echo "  $0 fraud                # Alert: score 80"
  echo "  $0 fraud 200000         # Alert: score 120 (80+40, both rules hit)"
  echo "  $0 normal               # No alert"
  echo "  $0 custom ACC-FRAUD PE-HIGH 5000  # Alert: score 140 (80+60)"
  echo ""
  echo "Env vars:"
  echo "  SQS_QUEUE_URL  SQS queue URL (default: fraud-transactions in ap-northeast-1)"
  echo "  AWS_REGION     AWS region (default: ap-northeast-1)"
  exit 1
}

case "${1:-}" in
  help|-h|--help)
    usage
    ;;
  fraud)
    ACCOUNT="ACC-BAD"
    PAYEE="PE-1"
    AMOUNT="${2:-5000}"
    ;;
  normal)
    ACCOUNT="NORMAL-ACC"
    PAYEE="PE-1"
    AMOUNT="${2:-500}"
    ;;
  custom)
    ACCOUNT="${2:?Missing accountId}"
    PAYEE="${3:?Missing payeeId}"
    AMOUNT="${4:?Missing amount}"
    ;;
  *)
    usage
    ;;
esac

TXN_ID="TXN-$(date +%s)"

PAYLOAD=$(cat <<EOF
{
  "transactionId": "$TXN_ID",
  "accountId": "$ACCOUNT",
  "payeeId": "$PAYEE",
  "amount": $AMOUNT
}
EOF
)

echo "Scenario: ${1}"
echo "Sending:  $PAYLOAD"

aws sqs send-message \
  --queue-url "$QUEUE_URL" \
  --message-body "$PAYLOAD" \
  --region "$REGION"

echo ""
echo "Done. Transaction ID: $TXN_ID"
echo ""
echo "Check logs:"
echo "  kubectl logs -n fraud-detection -l app=fraud-detection --tail=20 | grep $TXN_ID"
