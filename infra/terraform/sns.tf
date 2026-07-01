# ── SNS Topic for Fraud Alerts ──
resource "aws_sns_topic" "fraud_alerts" {
  name = "fraud-detection-alerts"

  tags = {
    Name        = "fraud-detection-alerts"
    Environment = var.environment
  }
}

# ── SNS Feedback Role (for delivery status logging) ──
resource "aws_iam_role" "sns_feedback" {
  name = "sns-fraud-alerts-feedback"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = { Service = "sns.amazonaws.com" }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "sns_feedback" {
  role = aws_iam_role.sns_feedback.name
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
      ]
      Resource = "*"
    }]
  })
}
