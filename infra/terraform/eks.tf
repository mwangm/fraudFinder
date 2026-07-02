# ── EKS Cluster (optional — set eks_cluster_name to skip) ──
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"
  count   = var.eks_cluster_name != "" ? 1 : 0

  cluster_name    = var.eks_cluster_name
  cluster_version = "1.32"

  vpc_id     = var.vpc_id
  subnet_ids = var.vpc_subnet_ids

  cluster_endpoint_public_access = true

  access_entries = {
    meng = {
      kubernetes_groups = []
      principal_arn     = "arn:aws:iam::774075583755:user/meng"
      policy_associations = {
        admin = {
          policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
          access_scope = {
            type = "cluster"
          }
        }
      }
    }
  }

  eks_managed_node_groups = {
    main = {
      instance_types = [var.eks_node_instance_type]
      min_size       = var.eks_min_nodes
      desired_size   = var.eks_desired_nodes
      max_size       = var.eks_max_nodes

      iam_role_additional_policies = {
        pod_policy = aws_iam_policy.eks_node_pod.arn
      }
    }
  }

  node_security_group_additional_rules = {
    metrics-server = {
      type                          = "ingress"
      protocol                      = "tcp"
      from_port                     = 10251
      to_port                       = 10251
      source_cluster_security_group = true
    }
  }

  cluster_addons = {
    metrics-server = {
      most_recent = true
    }
  }

  tags = {
    Environment = var.environment
  }
}

variable "vpc_subnet_ids" {
  description = "Subnet IDs for EKS (private subnets recommended)"
  type        = list(string)
  default     = []
}
