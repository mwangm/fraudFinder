# ── CloudWatch Log Group for ECS/EKS Containers ──
resource "aws_cloudwatch_log_group" "app" {
  name              = "/fraud-detection/app"
  retention_in_days = 30

  tags = {
    Name        = "fraud-detection"
    Environment = var.environment
  }
}

# ── CloudWatch Alarm: SQS DLQ depth ──
resource "aws_cloudwatch_metric_alarm" "sqs_dlq_depth" {
  alarm_name          = "fraud-detection-dlq-depth"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  alarm_description   = "Alarm when messages appear in the DLQ"
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = aws_sqs_queue.dlq.name
  }

  alarm_actions = [] # Add SNS topic ARN for notifications
}
