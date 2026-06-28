# 부하 테스트 전용 ephemeral 환경 (Phase 01)
# 3-tier: 생성기(k6) → SUT(앱+RDS) → 관측(otel-collector+prom/tempo/loki/grafana)
# apply 로 서고 destroy 로 통째 정리.

terraform {
  required_version = ">= 1.6"

  required_providers {
    aws    = { source = "hashicorp/aws", version = "~> 5.0" }
    random = { source = "hashicorp/random", version = "~> 3.0" }
  }

  # state: 로컬로 시작. 팀 공유/lock 필요하면 아래 S3 backend 로 교체.
  # backend "s3" {
  #   bucket         = "<state-bucket>"
  #   key            = "load-test/terraform.tfstate"
  #   region         = "ap-northeast-2"
  #   dynamodb_table = "<lock-table>"
  # }
}

provider "aws" {
  region = var.region
}

# EC2 AMI: Amazon Linux 2023 (x86_64). Graviton(t4g 등) 인스턴스면 variables 의
# ami_ssm_parameter 를 arm64 파라미터로 교체할 것.
data "aws_ssm_parameter" "ami" {
  name = var.ami_ssm_parameter
}

locals {
  name = "umc-loadtest"
  tags = {
    Project   = "umc-product"
    Purpose   = "load-test"
    ManagedBy = "terraform"
    Ephemeral = "true"
  }

  monitoring_private_ip = "10.20.1.10"
  sut_private_ip        = "10.20.1.20"
  generator_private_ip  = "10.20.1.30"

  grafana_port    = 13000
  prometheus_port = 19090
}
