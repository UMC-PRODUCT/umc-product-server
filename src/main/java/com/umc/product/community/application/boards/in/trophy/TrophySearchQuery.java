package com.umc.product.community.application.boards.in.trophy;

public record TrophySearchQuery(
        Integer week,   // 주차
        String school,  // 학교
        String part     // 파트
) {
}
