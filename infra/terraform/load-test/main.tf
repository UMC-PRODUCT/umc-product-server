# 부하 테스트 전용 ephemeral 환경 (Phase 01)
# 3-tier: 생성기(k6) → SUT(앱+RDS) → 관측(otel-collector+prom/tempo/loki/grafana)
# apply 로 테스트 환경을 만들고, 테스트가 끝나면 destroy 로 비용/데이터를 통째 정리한다.

terraform {
  required_version = ">= 1.6"

  # AWS 리소스와 랜덤 비밀번호만 만든다. 외부 모듈은 아직 쓰지 않아 구조를 단순하게 유지한다.
  required_providers {
    aws    = { source = "hashicorp/aws", version = "~> 5.0" }
    random = { source = "hashicorp/random", version = "~> 3.0" }
  }

  # 현재는 개인이 한 번 띄웠다가 내리는 부하 테스트용이라 local state 로 시작한다.
  # 여러 명이 동시에 apply/destroy 하거나 CI 에서 돌릴 계획이 생기면 S3 backend + DynamoDB lock 으로 교체한다.
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

# EC2 AMI: 역할별 instance type 이 서로 다른 CPU architecture 일 수 있다.
# 예: prod-like SUT 는 t4g.small(arm64), generator 는 c5.large(x86_64).
# AMI 를 하나로 공유하면 한쪽이 부팅되지 않으므로 역할별 SSM parameter 를 분리한다.
data "aws_ssm_parameter" "monitoring_ami" {
  name = var.monitoring_ami_ssm_parameter
}

data "aws_ssm_parameter" "sut_ami" {
  name = var.sut_ami_ssm_parameter
}

data "aws_ssm_parameter" "generator_ami" {
  name = var.generator_ami_ssm_parameter
}

locals {
  # 모든 리소스 이름 prefix. AWS 콘솔에서 ephemeral 부하 테스트 리소스를 쉽게 찾기 위한 값이다.
  name = "umc-loadtest"

  # 비용/정리 대상을 식별하기 위한 공통 tag. destroy 전에도 콘솔에서 Ephemeral=true 로 검색 가능하다.
  tags = {
    Project   = "umc-product"
    Purpose   = "load-test"
    ManagedBy = "terraform"
    Ephemeral = "true"
  }

  # Prometheus scrape target 과 app OTLP endpoint 를 user-data 렌더링 시점에 알아야 한다.
  # Terraform 리소스 간 private_ip 참조로 엮으면 monitoring ↔ SUT/generator 순환 의존이 생기므로 고정 IP 를 쓴다.
  monitoring_private_ip = "10.20.1.10"
  sut_private_ip        = "10.20.1.20"
  generator_private_ip  = "10.20.1.30"

  # 기존 monitoring compose 의 host port 매핑을 그대로 따른다.
  # 포트를 바꾸면 security group, outputs, user-data health check 를 함께 바꿔야 한다.
  grafana_port    = 13000
  prometheus_port = 19090
}
