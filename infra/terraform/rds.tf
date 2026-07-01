# ── RDS MySQL ──
resource "aws_db_subnet_group" "main" {
  name       = "fraud-detection-db-subnet"
  subnet_ids = var.database_subnet_ids

  tags = {
    Name = "fraud-detection-db-subnet"
  }
}

resource "aws_db_instance" "main" {
  identifier = "fraud-detection"

  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.db_instance_class

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 3306

  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = 20
  storage_encrypted     = false

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  publicly_accessible = true
  skip_final_snapshot = true

  backup_retention_period = 1
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  enabled_cloudwatch_logs_exports = ["error"]

  tags = {
    Name        = "fraud-detection"
    Environment = var.environment
  }
}

# Placeholder — set to your VPC subnet IDs
variable "database_subnet_ids" {
  description = "Subnet IDs for RDS"
  type        = list(string)
  default     = []
}

# ── Security Group for RDS ──
resource "aws_security_group" "rds" {
  name        = "fraud-detection-rds"
  description = "Allow MySQL from EKS nodes"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all (for testing)"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

variable "vpc_id" {
  description = "VPC ID for security groups"
  type        = string
  default     = ""
}
