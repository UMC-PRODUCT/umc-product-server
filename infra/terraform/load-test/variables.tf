# ── 리전 / AZ ────────────────────────────────────────────────
variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "az" {
  description = "모든 인스턴스·RDS를 둘 단일 AZ (네트워크 레이턴시 변수 제거)"
  type        = string
  default     = "ap-northeast-2a"
}

variable "az_secondary" {
  description = "RDS subnet group 요건(>=2 AZ)을 위한 보조 AZ. 인스턴스는 안 둠"
  type        = string
  default     = "ap-northeast-2c"
}

variable "ami_ssm_parameter" {
  description = "EC2 AMI SSM 파라미터. Graviton(arm64) 인스턴스면 .../al2023-ami-kernel-default-arm64 로 교체"
  type        = string
  default     = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

# ── 접근 ─────────────────────────────────────────────────────
variable "key_name" {
  description = "기존 EC2 키페어 이름 (SSH 접속용)"
  type        = string
}

variable "allowed_cidr" {
  description = "SSH·Grafana 접근 허용 CIDR (예: 내 공인 IP/32)"
  type        = string
}

# ── 인스턴스 타입 ────────────────────────────────────────────
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
variable "db_instance_class" {
  description = "RDS 인스턴스 클래스 (측정 대상 DB 스펙)"
  type        = string
  default     = "db.t4g.small"
}

variable "db_engine_version" {
  description = "RDS PostgreSQL 버전. `aws rds describe-db-engine-versions --engine postgres` 로 가용 버전 확인"
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
  EOT
  type        = string
  sensitive   = true
  default     = ""
}
