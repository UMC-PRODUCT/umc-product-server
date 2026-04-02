package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;

public record CancelBestWorkbookCommand(
        @NotNull(message = "챌린저 워크북 ID는 필수입니다")
        Long challengerWorkbookId,
        @NotNull(message = "요청자 멤버 ID는 필수입니다")
        Long memberId
) {
}
