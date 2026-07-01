# SUT EC2 가 부팅 중 Secrets Manager/ECR 을 직접 호출하기 위한 최소 IAM 권한.
# Terraform state 에 secret 원문을 넣지 않으려면 user-data 에 값 대신 ARN 만 전달해야 한다.
data "aws_iam_policy_document" "sut_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "sut_secret_access" {
  count = length(local.sut_secret_arns) > 0 ? 1 : 0

  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = local.sut_secret_arns
  }
}

data "aws_iam_policy_document" "sut_ecr_pull" {
  count = var.registry_type == "ecr" ? 1 : 0

  statement {
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
    ]
    resources = ["arn:aws:ecr:${var.region}:${data.aws_caller_identity.current.account_id}:repository/${var.ecr_repository_name}"]
  }
}

resource "aws_iam_role" "sut" {
  count = local.sut_needs_iam_role ? 1 : 0

  name               = "${local.name}-sut-secrets"
  assume_role_policy = data.aws_iam_policy_document.sut_assume_role.json

  tags = merge(local.tags, { Name = "${local.name}-sut-secrets" })
}

resource "aws_iam_role_policy" "sut_secret_access" {
  count = length(local.sut_secret_arns) > 0 ? 1 : 0

  name   = "${local.name}-sut-secret-access"
  role   = aws_iam_role.sut[0].id
  policy = data.aws_iam_policy_document.sut_secret_access[0].json
}

resource "aws_iam_role_policy" "sut_ecr_pull" {
  count = var.registry_type == "ecr" ? 1 : 0

  name   = "${local.name}-sut-ecr-pull"
  role   = aws_iam_role.sut[0].id
  policy = data.aws_iam_policy_document.sut_ecr_pull[0].json
}

resource "aws_iam_instance_profile" "sut" {
  count = local.sut_needs_iam_role ? 1 : 0

  name = "${local.name}-sut-secrets"
  role = aws_iam_role.sut[0].name

  tags = merge(local.tags, { Name = "${local.name}-sut-secrets" })
}
