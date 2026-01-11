package com.umc.product.community.application.port.in.trophy;

public record CreateTrophyCommand(
        Long challengerId,
        Integer week,
        String title,
        String content,
        String url
) {
}
