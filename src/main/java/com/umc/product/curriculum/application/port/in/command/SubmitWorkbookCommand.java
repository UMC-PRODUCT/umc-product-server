package com.umc.product.curriculum.application.port.in.command;

import jakarta.validation.constraints.NotNull;

/**
 * 워크북 제출 커맨드
 *
 * @param originalWorkbookId   제출 대상 원본 워크북 ID
 * @param challengerId         제출하는 챌린저 ID
 * @param submission           제출 내용 (링크 또는 메모, PLAIN 타입인 경우 null 가능)
 */
public record SubmitWorkbookCommand(
        @NotNull(message = "원본 워크북 ID는 필수입니다")
        Long originalWorkbookId,
        @NotNull(message = "챌린저 ID는 필수입니다")
        Long challengerId,
        String submission
) {
}
