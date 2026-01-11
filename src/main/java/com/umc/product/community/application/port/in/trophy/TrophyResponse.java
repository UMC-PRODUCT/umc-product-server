package com.umc.product.community.application.port.in.trophy;

public record TrophyResponse(
        Long trophyId,
        int week,
        String challengerName,
        String school,
        String part,
        String title,
        String content,
        String url
) {
    // 챌린저 파트 보고 빌드파트까지 진행
}

