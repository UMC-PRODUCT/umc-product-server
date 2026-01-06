package com.umc.product.curriculum.application.port.in.dto;

public record SelectBestWorkbookCommand(
        Long challengerWorkbookId,
        String recommendation // 추천사
) {
}
