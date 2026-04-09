package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 챌린저 워크북 ID 기반 워크북 제출 커맨드
 *
 * @param challengerWorkbookId 제출 대상 챌린저 워크북 ID
 * @param memberId             요청한 사용자의 멤버 ID (본인 확인용)
 * @param submission           제출 내용 (링크 또는 메모, PLAIN 타입인 경우 null 가능)
 */
public record SubmitChallengerWorkbookCommand(
        @NotNull(message = "챌린저 워크북 ID는 필수입니다")
        Long challengerWorkbookId,
        @NotNull(message = "멤버 ID는 필수입니다")
        Long memberId,
        String submission
) {
}
