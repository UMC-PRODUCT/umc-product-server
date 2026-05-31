package com.umc.product.project.application.port.in.command.dto;

import lombok.Builder;

/**
 * 챌린저 지원서 철회 Command (APPLY-005).
 * <p>
 * {@code requesterMemberId}는 철회를 수행하는 회원의 ID입니다.
 * 행위자 적격성 (본인 여부 / 운영진 등) 검증은 호출하는 endpoint의 권한 모델에서 결정합니다.
 */
@Builder
public record CancelProjectApplicationCommand(
    Long applicationId,
    Long requesterMemberId,
    String reason
) {
}
