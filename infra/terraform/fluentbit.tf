# ── Fluent Bit via Helm (aws-for-fluent-bit) ──
resource "helm_release" "aws_for_fluent_bit" {
  name       = "aws-for-fluent-bit"
  chart      = "aws-for-fluent-bit"
  repository = "https://aws.github.io/eks-charts"
  version    = "0.2.0"
  namespace  = "kube-system"

  set {
    name  = "cloudWatchLogs.logGroupName"
    value = "/fraud-detection/app"
  }

  set {
    name  = "cloudWatchLogs.region"
    value = var.aws_region
  }

  recreate_pods = true

  depends_on = [module.eks]
}
