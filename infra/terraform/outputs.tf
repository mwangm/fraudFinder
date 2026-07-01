output "sqs_queue_url" {
  value = aws_sqs_queue.main.url
}

output "sqs_dlq_url" {
  value = aws_sqs_queue.dlq.url
}

output "sqs_queue_arn" {
  value = aws_sqs_queue.main.arn
}

output "sns_topic_arn" {
  value = aws_sns_topic.fraud_alerts.arn
}

output "rds_endpoint" {
  value = aws_db_instance.main.endpoint
}

output "rds_port" {
  value = aws_db_instance.main.port
}

output "github_actions_role_arn" {
  value = aws_iam_role.github_actions.arn
}

output "eks_cluster_endpoint" {
  value = var.eks_cluster_name != "" ? module.eks[0].cluster_endpoint : null
}

output "eks_cluster_name" {
  value = var.eks_cluster_name != "" ? module.eks[0].cluster_name : null
}
