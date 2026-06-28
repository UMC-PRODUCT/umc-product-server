# ── VPC / 서브넷 / 라우팅 ────────────────────────────────────
# 부하 테스트끼리 충돌하지 않도록 독립 VPC 를 만든다.
# 기존 dev/prod 네트워크를 재사용하지 않아 라우팅, SG, 데이터가 운영 환경과 섞이지 않는다.
resource "aws_vpc" "this" {
  cidr_block           = "10.20.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = merge(local.tags, { Name = "${local.name}-vpc" })
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = merge(local.tags, { Name = "${local.name}-igw" })
}

# EC2 가 사는 public subnet. Docker image / package 다운로드를 위해 인터넷 egress 가 필요하다.
# NAT Gateway 를 두면 비용과 구성이 늘어나므로 v1 에서는 EC2 에 public IP 를 부여하고 SG 로 inbound 를 제한한다.
resource "aws_subnet" "primary" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.20.1.0/24"
  availability_zone       = var.az
  map_public_ip_on_launch = true
  tags                    = merge(local.tags, { Name = "${local.name}-subnet-primary" })
}

# RDS subnet group 은 >=2 AZ 필요. DB 는 public route table 에 연결하지 않는다.
# private subnet 에 route table association 을 만들지 않아 인터넷에서 DB 로 직접 접근할 경로를 만들지 않는다.
resource "aws_subnet" "rds_primary" {
  vpc_id            = aws_vpc.this.id
  cidr_block        = "10.20.11.0/24"
  availability_zone = var.az
  tags              = merge(local.tags, { Name = "${local.name}-subnet-rds-primary" })
}

resource "aws_subnet" "rds_secondary" {
  vpc_id            = aws_vpc.this.id
  cidr_block        = "10.20.12.0/24"
  availability_zone = var.az_secondary
  tags              = merge(local.tags, { Name = "${local.name}-subnet-rds-secondary" })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
  tags = merge(local.tags, { Name = "${local.name}-rt" })
}

# public subnet 의 EC2 만 인터넷으로 나간다. RDS subnet 은 이 route table 에 연결하지 않는다.
resource "aws_route_table_association" "primary" {
  subnet_id      = aws_subnet.primary.id
  route_table_id = aws_route_table.public.id
}

# ── Security Groups ──────────────────────────────────────────
# SG 끼리 상호 참조 → cycle 방지 위해 SG 본체는 egress 만, 인바운드는 별도 rule 로.
# 각 SG 의 egress 는 package/image 다운로드와 OTLP/DB 통신을 단순화하기 위해 전체 허용한다.
# 실제 노출면은 아래 ingress rule 들이 결정한다.
resource "aws_security_group" "generator" {
  name_prefix = "${local.name}-gen-"
  description = "k6 부하 생성기"
  vpc_id      = aws_vpc.this.id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.name}-gen" })
}

resource "aws_security_group" "sut" {
  name_prefix = "${local.name}-sut-"
  description = "측정 대상 앱"
  vpc_id      = aws_vpc.this.id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.name}-sut" })
}

resource "aws_security_group" "rds" {
  name_prefix = "${local.name}-rds-"
  description = "RDS - SUT and monitoring exporter only"
  vpc_id      = aws_vpc.this.id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.name}-rds" })
}

resource "aws_security_group" "monitoring" {
  name_prefix = "${local.name}-mon-"
  description = "관측 스택"
  vpc_id      = aws_vpc.this.id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.name}-mon" })
}

# SSH (전 인스턴스) — 내 IP 에서만
# SSM Session Manager 를 붙이면 22번을 닫을 수 있다. 지금은 빠른 디버깅을 위해 allowed_cidr 에만 연다.
resource "aws_security_group_rule" "ssh_generator" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.generator.id
}
resource "aws_security_group_rule" "ssh_sut" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.sut.id
}
resource "aws_security_group_rule" "ssh_monitoring" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.monitoring.id
}

# SUT: 8080 ← 생성기/관리자, 9090(management) ← 관측/관리자, 9100(node) ← 관측
# 8080 은 실제 k6 트래픽 진입점이다. 관리자 CIDR 은 smoke/debug curl 용도로만 허용한다.
resource "aws_security_group_rule" "sut_app_from_gen" {
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.generator.id
  security_group_id        = aws_security_group.sut.id
}
resource "aws_security_group_rule" "sut_app_from_me" {
  type              = "ingress"
  from_port         = 8080
  to_port           = 8080
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.sut.id
}
resource "aws_security_group_rule" "sut_mgmt_from_mon" {
  type                     = "ingress"
  from_port                = 9090
  to_port                  = 9090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.monitoring.id
  security_group_id        = aws_security_group.sut.id
}
resource "aws_security_group_rule" "sut_mgmt_from_me" {
  type              = "ingress"
  from_port         = 9090
  to_port           = 9090
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.sut.id
}
resource "aws_security_group_rule" "sut_node_from_mon" {
  type                     = "ingress"
  from_port                = 9100
  to_port                  = 9100
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.monitoring.id
  security_group_id        = aws_security_group.sut.id
}

# 생성기: 9100(node) ← 관측
# generator 도 병목이 될 수 있으므로 node-exporter 로 CPU/메모리/네트워크를 같이 본다.
resource "aws_security_group_rule" "generator_node_from_mon" {
  type                     = "ingress"
  from_port                = 9100
  to_port                  = 9100
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.monitoring.id
  security_group_id        = aws_security_group.generator.id
}

# RDS: 5432 ← SUT + postgres_exporter(관측)
# monitoring 에서 직접 DB 를 scrape 해야 pg connection/lock/cache hit 지표를 같은 시간축에 올릴 수 있다.
resource "aws_security_group_rule" "rds_from_sut" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.sut.id
  security_group_id        = aws_security_group.rds.id
}
resource "aws_security_group_rule" "rds_from_monitoring" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.monitoring.id
  security_group_id        = aws_security_group.rds.id
}

# 관측: OTLP(4317/4318) ← SUT, Prometheus(19090) ← 생성기+내IP, Grafana(13000) ← 내IP
# (포트는 infra/monitoring/grafana/docker-compose.yml 의 호스트 매핑 기준)
# 4317/4318 은 앱이 metrics/traces/logs 를 collector 로 push 하는 경로다.
resource "aws_security_group_rule" "mon_otlp_from_sut" {
  type                     = "ingress"
  from_port                = 4317
  to_port                  = 4318
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.sut.id
  security_group_id        = aws_security_group.monitoring.id
}
resource "aws_security_group_rule" "mon_prom_from_gen" {
  type                     = "ingress"
  from_port                = 19090
  to_port                  = 19090
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.generator.id
  security_group_id        = aws_security_group.monitoring.id
}
resource "aws_security_group_rule" "mon_prom_from_me" {
  type              = "ingress"
  from_port         = 19090
  to_port           = 19090
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.monitoring.id
}
resource "aws_security_group_rule" "mon_grafana_from_me" {
  type              = "ingress"
  from_port         = 13000
  to_port           = 13000
  protocol          = "tcp"
  cidr_blocks       = [var.allowed_cidr]
  security_group_id = aws_security_group.monitoring.id
}
