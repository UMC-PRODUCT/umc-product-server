package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectApplicationCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 챌린저 지원서 Draft 생성 요청 (APPLY-001).
 * <p>
 * FE에서 지원할 매칭 차수를 명시한다. 서버는 해당 차수의 오픈 여부와 내 파트 타입 일치 여부를 검증한다.
 * 지원 가능한 차수 목록은 MATCHING-001 API로 사전 조회한다.
 */
@Builder
public record CreateProjectApplicationRequest(
    @NotNull(message = "matchingRoundId는 필수입니다")
    Long matchingRoundId
) {

    public CreateDraftProjectApplicationCommand toCommand(Long projectId, Long applicantMemberId) {
        return CreateDraftProjectApplicationCommand.builder()
            .projectId(projectId)
            .applicantMemberId(applicantMemberId)
            .matchingRoundId(matchingRoundId)
            .build();
    }
}
