package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 챌린저 지원서 Draft 생성 Command (APPLY-001).
 * <p>
 * 동일 (projectId, applicantMemberId) 조합에 PENDING 지원서가 이미 있으면 Service 단에서 멱등 처리한다.
 */
@Builder
public record CreateDraftProjectApplicationCommand(
    Long projectId,
    Long applicantMemberId
) {
}
