package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 챌린저 워크북 인정 처리 커맨드 (운영진 전용)
 * <p>
 * 인정 처리된 워크북은 미션을 제출하지 않아도 벌점이 부과되지 않습니다.
 *
 * @param challengerWorkbookId   인정 처리 대상 챌린저 워크북 ID
 * @param excuseApprovedMemberId 요청 운영진의 멤버 ID (권한 확인용)
 * @param reason                 인정 처리 사유 (필수)
 */
@Builder
public record ExcuseChallengerWorkbookCommand(
    @NotNull(message = "챌린저 워크북 ID는 필수입니다")
    Long challengerWorkbookId,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long excuseApprovedMemberId,

    @NotBlank(message = "인정 사유는 필수입니다.")
    String reason
) {
}
