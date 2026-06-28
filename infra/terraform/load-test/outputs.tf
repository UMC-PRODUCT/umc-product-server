output "sut_public_ip" {
  description = "SUT 공인 IP"
  value       = aws_instance.sut.public_ip
}

output "sut_app_url" {
  description = "앱 엔드포인트 (생성기는 private IP 로 접근, 이건 디버그용)"
  value       = "http://${aws_instance.sut.public_ip}:8080"
}

output "sut_private_ip" {
  value = aws_instance.sut.private_ip
}

output "grafana_url" {
  description = "관측 Grafana (allowed_cidr 에서만 접근)"
  value       = "http://${aws_instance.monitoring.public_ip}:${local.grafana_port}"
}

output "prometheus_url" {
  description = "관측 Prometheus (allowed_cidr 에서만 접근)"
  value       = "http://${aws_instance.monitoring.public_ip}:${local.prometheus_port}"
}

output "grafana_admin_user" {
  value = "admin"
}

output "grafana_admin_password" {
  value     = random_password.grafana_admin.result
  sensitive = true
}

output "generator_ssh" {
  description = "생성기 SSH (여기서 k6 실행)"
  value       = "ssh ec2-user@${aws_instance.generator.public_ip}"
}

output "rds_endpoint" {
  value = aws_db_instance.this.endpoint
}

output "db_password" {
  description = "RDS 비밀번호 (랜덤 생성)"
  value       = random_password.db.result
  sensitive   = true
}

output "next_steps" {
  value = <<-EOT
    1) SUT 헬스: curl http://${aws_instance.sut.public_ip}:9090/actuator/health  (또는 SSH 후 로컬)
    2) Grafana: http://${aws_instance.monitoring.public_ip}:${local.grafana_port}
       비밀번호: terraform output -raw grafana_admin_password
    3) 생성기 접속 후 k6 실행:
       ssh ec2-user@${aws_instance.generator.public_ip}
       run-umc-k6 smoke 1
    4) 끝나면: terraform destroy
  EOT
}
