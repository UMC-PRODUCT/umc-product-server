package com.umc.product.project.application.service.policy;

import java.time.Instant;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param openUntil 이 시각 전에는 매칭 차수가 시작됐어도 TO를 수정할 수 있다. 비어 있으면 연장하지 않는다.
 * @deprecated 지원 중단: 대체 없음. 삭제 예정: 2026-11-15 릴리스, 담당자: wlgusqkr
 */
@Deprecated(since = "2026-11-12", forRemoval = true)
@ConfigurationProperties(prefix = "app.project.quota-edit")
public record ProjectQuotaEditProperties(Instant openUntil) {
}
