# ── 리전 / AZ ────────────────────────────────────────────────
# 모든 기본값은 서울 리전 기준이다. 다른 리전에서 실행하면 AMI, AZ, RDS engine version 가용성을 같이 확인해야 한다.
variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "az" {
  description = "EC2와 RDS primary를 둘 AZ. 네트워크 레이턴시 변수를 줄이기 위해 한 AZ에 모은다."
  type        = string
  default     = "ap-northeast-2a"
}

variable "az_secondary" {
  description = "RDS subnet group 요건(>=2 AZ)을 위한 보조 AZ. EC2는 두지 않는다."
  type        = string
  default     = "ap-northeast-2c"
}

variable "ami_ssm_parameter" {
  description = "EC2 AMI SSM 파라미터. Graviton(arm64) 인스턴스면 .../al2023-ami-kernel-default-arm64 로 교체"
  type        = string
  default     = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

# ── 접근 ─────────────────────────────────────────────────────
# allowed_cidr 은 public endpoint 의 유일한 외부 접근 제어다.
# 0.0.0.0/0 으로 열면 Grafana/Prometheus/SUT debug port 가 인터넷에 노출되므로 금지한다.
variable "key_name" {
  description = "기존 EC2 키페어 이름 (SSH 접속용)"
  type        = string
}

variable "allowed_cidr" {
  description = "SSH·Grafana 접근 허용 CIDR (예: 내 공인 IP/32)"
  type        = string
}

# ── 인스턴스 타입 ────────────────────────────────────────────
# SUT 타입은 측정 대상 스펙 자체라 임의로 키우면 안 된다.
# 반대로 generator/monitoring 은 측정 대상이 아니므로 먼저 병목나지 않게 SUT보다 여유 있게 잡는다.
variable "sut_instance_type" {
  description = "SUT(앱) — prod 앱과 동일 타입으로 (현재 스펙 fidelity 핵심)"
  type        = string
  default     = "t3.small"
}

variable "generator_instance_type" {
  description = "k6 부하 생성기 — 생성기가 먼저 병목나지 않게 넉넉히"
  type        = string
  default     = "c5.large"
}

variable "monitoring_instance_type" {
  description = "관측 스택(otel-collector+prom+tempo+loki+grafana)"
  type        = string
  default     = "t3.large"
}

# ── RDS ──────────────────────────────────────────────────────
# 이 블록은 "측정하고 싶은 DB 스펙"을 정의한다. 현재 이슈 기준 목표는 db.t4g.small 이다.
variable "db_instance_class" {
  description = "RDS 인스턴스 클래스 (측정 대상 DB 스펙)"
  type        = string
  default     = "db.t4g.small"
}

variable "db_engine_version" {
  description = "RDS PostgreSQL 버전. apply 전에 해당 리전에서 가용한 정확한 버전 문자열을 확인한다."
  type        = string
  default     = "16.4"
}

variable "db_allocated_storage" {
  description = "RDS 스토리지(GB)"
  type        = number
  default     = 50
}

variable "db_name" {
  type    = string
  default = "umc_product"
}

variable "db_username" {
  type    = string
  default = "umc_product"
}

# ── 앱 이미지 / 배포 ─────────────────────────────────────────
# app_image 는 이미 registry 에 push 된 dev profile 실행 가능 이미지를 가리켜야 한다.
# Terraform 은 이미지를 빌드하지 않고 EC2 에서 docker compose pull 만 수행한다.
variable "app_image" {
  description = "앱 Docker 이미지 (예: docker.io/<repo>:<dev-tag>)"
  type        = string
}

variable "registry_server" {
  description = "컨테이너 레지스트리 (docker.io 또는 ghcr.io)"
  type        = string
  default     = "docker.io"
}

variable "registry_username" {
  description = "레지스트리 로그인 사용자명. public image면 빈 문자열."
  type        = string
  sensitive   = true
  default     = ""
}

variable "registry_password" {
  description = "레지스트리 로그인 토큰/비밀번호. public image면 빈 문자열."
  type        = string
  sensitive   = true
  default     = ""
}

variable "git_repo_url" {
  description = "compose/관측 config 와 k6 스크립트를 가져올 레포 URL. private repo면 read-only 토큰을 포함한 HTTPS URL."
  type        = string

  # monitoring EC2 는 infra/monitoring/grafana 를, generator EC2 는 docs/guides/load-test/k6 를 clone 해서 사용한다.
  # 지금 구조에서는 이 값이 비면 EC2 가 필요한 파일을 받을 수 없다.
  validation {
    condition     = length(trimspace(var.git_repo_url)) > 0
    error_message = "git_repo_url 은 필수입니다. 현재 구조는 EC2 user-data 에서 레포를 clone 해 monitoring config 와 k6 script 를 가져옵니다."
  }
}

variable "app_env" {
  description = <<-EOT
    앱 런타임 env(app.env) 내용 (dev 프로필 기준, 멀티라인).
    DATABASE_URL/USERNAME/PASSWORD · OTEL_URL · SPRING_PROFILES_ACTIVE · HIKARI_MAX_POOL_SIZE 는
    user_data 가 부하테스트 값으로 덮어쓰므로 여기엔 그 외 부팅 필수 env(외부 연동 등)만 넣으면 됨.
    주의: 현재 방식은 Terraform state 와 EC2 user-data 에 값이 남는다. 운영 수준 보안이 필요하면 Secrets Manager ARN 방식으로 바꾼다.
  EOT
  type        = string
  sensitive   = true
  default     = ""
}
