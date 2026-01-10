package com.umc.product.community.application.port.in.trophy;

public record CreateTrophyCommand(
        Long challengerId,
        int week,
        String title,
        String content,
        String url
) {
}
