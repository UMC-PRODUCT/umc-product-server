resource "aws_db_subnet_group" "this" {
  name       = "${local.name}-db"
  subnet_ids = [aws_subnet.rds_primary.id, aws_subnet.rds_secondary.id]
  tags       = merge(local.tags, { Name = "${local.name}-db" })
}

resource "random_password" "db" {
  length  = 24
  special = false # 일부 클라이언트 URL 인코딩 이슈 회피
}

resource "aws_db_instance" "this" {
  identifier     = "${local.name}-pg"
  engine         = "postgres"
  engine_version = var.db_engine_version
  instance_class = var.db_instance_class

  allocated_storage = var.db_allocated_storage
  storage_type      = "gp3"

  db_name  = var.db_name
  username = var.db_username
  password = random_password.db.result

  availability_zone      = var.az # SUT 와 같은 AZ
  multi_az               = false
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  # ephemeral: 빠른 생성/삭제
  skip_final_snapshot = true
  deletion_protection = false
  apply_immediately   = true

  tags = merge(local.tags, { Name = "${local.name}-pg" })

  # ⚠️ 크레딧 모드(unlimited): aws_db_instance 에 직접 인자가 없다.
  #   db.t4g 는 RDS 기본이 unlimited 인 경우가 많으나, 생성 후
  #   `aws rds describe-db-instances` 로 ProcessorFeatures/credit 확인하고
  #   필요하면 콘솔/CLI(modify-db-instance)로 unlimited 설정할 것.
  # ⚠️ PostGIS: 앱 Flyway 마이그레이션이 CREATE EXTENSION 으로 만든다(별도 작업 불필요).
}
