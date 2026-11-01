package com.umc.product.project.application.service.evaluator;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SUPER_ADMIN 전용 운영 토글.
 * <p>
 * {@code allowDraftRead} 가 true 면 SUPER_ADMIN 이 DRAFT 상태의 프로젝트/지원서 단건을 조회할 수 있다. 초기 배포 모니터링용으로만 켜고
 * 안정화 후 끈다. 기본 false.
 */
@ConfigurationProperties(prefix = "app.super-admin")
public record SuperAdminProperties(boolean allowDraftRead) {
}
