package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 챌린저 워크북 수정 커맨드
 *
 * @param challengerWorkbookId 수정 대상 챌린저 워크북 ID
 * @param requesterMemberId    요청 사용자의 멤버 ID (본인 확인용)
 * @param content              변경할 워크북 내용
 */
@Builder
public record EditChallengerWorkbookCommand(
    @NotNull(message = "챌린저 워크북 ID는 필수입니다")
    Long challengerWorkbookId,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long requesterMemberId,

    @NotBlank(message = "수정할 내용은 필수입니다.")
    String content
) {
}
