# 관측 EC2 — 먼저 떠야 SUT 가 OTLP 를 보낼 수 있음 (otel-collector 토큰 공유)
resource "random_password" "otel_token" {
  length  = 32
  special = false
}

resource "random_password" "grafana_admin" {
  length  = 20
  special = false
}

resource "aws_instance" "monitoring" {
  ami                         = data.aws_ssm_parameter.ami.value
  instance_type               = var.monitoring_instance_type
  subnet_id                   = aws_subnet.primary.id
  private_ip                  = local.monitoring_private_ip
  vpc_security_group_ids      = [aws_security_group.monitoring.id]
  key_name                    = var.key_name
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/user-data/monitoring.sh.tftpl", {
    git_repo_url           = var.git_repo_url
    otel_token             = random_password.otel_token.result
    grafana_admin_password = random_password.grafana_admin.result
    db_host                = aws_db_instance.this.address
    db_port                = aws_db_instance.this.port
    db_name                = var.db_name
    db_username            = var.db_username
    db_password            = random_password.db.result
    sut_private_ip         = local.sut_private_ip
    generator_private_ip   = local.generator_private_ip
  })

  dynamic "credit_specification" {
    for_each = startswith(var.monitoring_instance_type, "t") ? [1] : []
    content { cpu_credits = "unlimited" }
  }

  root_block_device {
    volume_size = 60 # tempo/loki chunk 여유
    volume_type = "gp3"
  }

  tags = merge(local.tags, { Name = "${local.name}-monitoring" })
}

# SUT — 앱(dev) + valkey, DB 는 RDS
resource "aws_instance" "sut" {
  ami                         = data.aws_ssm_parameter.ami.value
  instance_type               = var.sut_instance_type
  subnet_id                   = aws_subnet.primary.id
  private_ip                  = local.sut_private_ip
  vpc_security_group_ids      = [aws_security_group.sut.id]
  key_name                    = var.key_name
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/user-data/sut.sh.tftpl", {
    app_image         = var.app_image
    registry_server   = var.registry_server
    registry_username = var.registry_username
    registry_password = var.registry_password
    db_host           = aws_db_instance.this.address
    db_port           = aws_db_instance.this.port
    db_name           = var.db_name
    db_username       = var.db_username
    db_password       = random_password.db.result
    otel_host         = local.monitoring_private_ip
    otel_token        = random_password.otel_token.result
    app_env           = var.app_env
    hikari_pool       = 4
  })

  dynamic "credit_specification" {
    for_each = startswith(var.sut_instance_type, "t") ? [1] : []
    content { cpu_credits = "unlimited" }
  }

  tags = merge(local.tags, { Name = "${local.name}-sut" })

  depends_on = [aws_db_instance.this, aws_instance.monitoring]
}

# 생성기 — k6 설치 (실행은 수동)
resource "aws_instance" "generator" {
  ami                         = data.aws_ssm_parameter.ami.value
  instance_type               = var.generator_instance_type
  subnet_id                   = aws_subnet.primary.id
  private_ip                  = local.generator_private_ip
  vpc_security_group_ids      = [aws_security_group.generator.id]
  key_name                    = var.key_name
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/user-data/generator.sh.tftpl", {
    git_repo_url = var.git_repo_url
    sut_host     = local.sut_private_ip
    prom_rw_url  = "http://${local.monitoring_private_ip}:${local.prometheus_port}/api/v1/write"
  })

  dynamic "credit_specification" {
    for_each = startswith(var.generator_instance_type, "t") ? [1] : []
    content { cpu_credits = "unlimited" }
  }

  tags = merge(local.tags, { Name = "${local.name}-generator" })

  depends_on = [aws_instance.sut, aws_instance.monitoring]
}
