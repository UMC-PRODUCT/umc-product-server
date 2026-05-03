package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 챌린저 지원서 최종 제출 Command (APPLY-003).
 * <p>
 * (projectId, requesterMemberId) 로 본인의 PENDING 지원서를 식별 — applicationId 를 path 에 노출하지 않는다.
 * PENDING -> SUBMITTED 전이. 필수 답변 누락 검증은 Survey {@code ManageFormResponseUseCase.submitDraft}가 담당.
 */
@Builder
public record SubmitProjectApplicationCommand(
    Long projectId,
    Long requesterMemberId
) {
}
