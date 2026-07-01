variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

# ── ECR ──
variable "ecr_repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "fraud-detection"
}

# ── SQS ──
variable "sqs_queue_name" {
  description = "SQS main queue name"
  type        = string
  default     = "fraud-transactions"
}

variable "sqs_dlq_name" {
  description = "SQS dead-letter queue name"
  type        = string
  default     = "fraud-transactions-dlq"
}

variable "sqs_visibility_timeout_seconds" {
  description = "SQS visibility timeout"
  type        = number
  default     = 120
}

variable "sqs_max_receive_count" {
  description = "Max receives before moving to DLQ"
  type        = number
  default     = 3
}

# ── RDS ──
variable "db_name" {
  description = "Database name"
  type        = string
  default     = "fraud_finder"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "fraudFinder"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage (GB)"
  type        = number
  default     = 20
}

# ── EKS ──
variable "eks_cluster_name" {
  description = "EKS cluster name (leave empty to skip EKS creation)"
  type        = string
  default     = "fraud-detection"
}

variable "eks_node_instance_type" {
  description = "EKS node instance type"
  type        = string
  default     = "t3.micro"
}

variable "eks_desired_nodes" {
  description = "Desired number of EKS nodes"
  type        = number
  default     = 2
}

variable "eks_min_nodes" {
  description = "Minimum number of EKS nodes"
  type        = number
  default     = 1
}

variable "eks_max_nodes" {
  description = "Maximum number of EKS nodes"
  type        = number
  default     = 4
}

# ── GitHub OIDC ──
variable "github_org" {
  description = "GitHub organization or username"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
  default     = "fraudFinder"
}
