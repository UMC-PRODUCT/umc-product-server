package com.umc.product.project.application.port.in.command.dto;

import lombok.Builder;

/**
 * 챌린저 지원서 Draft 생성 Command (APPLY-001).
 * <p>
 * FE에서 지원할 매칭 차수를 명시하고, 서버는 해당 차수의 오픈 여부와 파트 타입을 검증한다.
 * 동일 (projectId, applicantMemberId, matchingRoundId) 조합에 DRAFT 지원서가 이미 있으면 Service 단에서 멱등 처리한다.
 */
@Builder
public record CreateDraftProjectApplicationCommand(
    Long projectId,
    Long applicantMemberId,
    Long matchingRoundId
) {
}
