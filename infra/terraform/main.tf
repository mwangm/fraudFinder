terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_eks_cluster" "main" {
  count = var.eks_cluster_name != "" ? 1 : 0
  name  = var.eks_cluster_name
}

data "aws_eks_cluster_auth" "main" {
  count = var.eks_cluster_name != "" ? 1 : 0
  name  = var.eks_cluster_name
}

provider "kubernetes" {
  host                   = var.eks_cluster_name != "" ? data.aws_eks_cluster.main[0].endpoint : ""
  cluster_ca_certificate = var.eks_cluster_name != "" ? base64decode(data.aws_eks_cluster.main[0].certificate_authority[0].data) : ""
  token                  = var.eks_cluster_name != "" ? data.aws_eks_cluster_auth.main[0].token : ""
}

provider "helm" {
  kubernetes {
    host                   = var.eks_cluster_name != "" ? data.aws_eks_cluster.main[0].endpoint : ""
    cluster_ca_certificate = var.eks_cluster_name != "" ? base64decode(data.aws_eks_cluster.main[0].certificate_authority[0].data) : ""
    token                  = var.eks_cluster_name != "" ? data.aws_eks_cluster_auth.main[0].token : ""
  }
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
