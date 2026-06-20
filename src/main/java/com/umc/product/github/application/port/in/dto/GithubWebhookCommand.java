package com.umc.product.github.application.port.in.dto;

/**
 * 서명 검증을 통과한 GitHub 웹훅 1건을 표현하는 커맨드.
 *
 * @param eventType  {@code X-GitHub-Event} 헤더 값 (예: {@code pull_request}, {@code issues})
 * @param deliveryId {@code X-GitHub-Delivery} 헤더 값 (웹훅 1건의 UUID). Phase A 는 멱등 처리에 사용하지 않고
 *                   로깅/추적용으로만 보관한다 (중복 수신 시 약간의 drift 허용 — ADR-010 Phase B 에서 테이블로 정확 멱등 처리).
 * @param payload    원본 JSON 본문 (서명 검증에 사용된 바이트와 동일한 내용)
 */
public record GithubWebhookCommand(
    String eventType,
    String deliveryId,
    String payload
) {
}
