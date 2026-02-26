package com.umc.product.curriculum.application.port.in.command;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 워크북 제출 커맨드
 *
 * @param challengerWorkbookId 제출 대상 챌린저 워크북 ID
 * @param submission           제출 내용 (링크 또는 메모, PLAIN 타입인 경우 null 가능)
 */
public record SubmitWorkbookCommand(
        @NotNull(message = "챌린저 워크북 ID는 필수입니다")
        Long challengerWorkbookId,
        String submission
) {
}
