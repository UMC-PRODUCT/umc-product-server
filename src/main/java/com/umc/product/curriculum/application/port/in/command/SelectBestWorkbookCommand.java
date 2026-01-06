package com.umc.product.curriculum.application.port.in.command;

public record SelectBestWorkbookCommand(
        Long challengerWorkbookId,
        String recommendation // 추천사
) {
}
