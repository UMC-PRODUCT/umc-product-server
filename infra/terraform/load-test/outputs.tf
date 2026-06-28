# output 은 apply 직후 smoke/debug 에 필요한 접속 정보만 노출한다.
# 비밀번호류는 sensitive 로 표시되지만 state 에는 남으므로 state 파일 관리는 별도로 주의한다.
output "sut_public_ip" {
  description = "SUT 공인 IP"
  value       = aws_instance.sut.public_ip
}

output "sut_app_url" {
  description = "관리자 CIDR 에서만 쓰는 디버그용 앱 엔드포인트. k6 는 private IP 로 접근한다."
  value       = "http://${aws_instance.sut.public_ip}:8080"
}

output "sut_private_ip" {
  description = "generator 가 실제 부하를 보내는 SUT private IP"
  value       = aws_instance.sut.private_ip
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
  description = "Grafana 초기 admin password. `terraform output -raw grafana_admin_password` 로 조회한다."
  value       = random_password.grafana_admin.result
  sensitive   = true
}

output "generator_ssh" {
  description = "생성기 SSH (여기서 k6 실행)"
  value       = "ssh ec2-user@${aws_instance.generator.public_ip}"
}

output "rds_endpoint" {
  description = "RDS endpoint. 앱과 postgres_exporter 가 같은 DB 를 바라보는지 확인할 때 사용한다."
  value       = aws_db_instance.this.endpoint
}

output "db_password" {
  description = "RDS 비밀번호 (랜덤 생성)"
  value       = random_password.db.result
  sensitive   = true
}

output "next_steps" {
  description = "apply 직후 사람이 실행할 최소 검증 순서"
  value       = <<-EOT
    1) SUT 헬스: curl http://${aws_instance.sut.public_ip}:9090/actuator/health  (또는 SSH 후 로컬)
    2) Grafana: http://${aws_instance.monitoring.public_ip}:${local.grafana_port}
       비밀번호: terraform output -raw grafana_admin_password
    3) 생성기 접속 후 k6 실행:
       ssh ec2-user@${aws_instance.generator.public_ip}
       run-umc-k6 smoke 1
    4) 끝나면: terraform destroy
  EOT
}
