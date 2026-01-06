package com.umc.product.curriculum.application.port.in.command;

import jakarta.validation.constraints.NotNull;

public record SelectBestWorkbookCommand(
        @NotNull(message = "챌린저 워크북 ID는 필수입니다")
        Long challengerWorkbookId,
        String recommendation // 추천사
) {
}
