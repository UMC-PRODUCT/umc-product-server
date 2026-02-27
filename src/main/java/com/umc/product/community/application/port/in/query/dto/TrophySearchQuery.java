package com.umc.product.community.application.port.in.query.dto;

public record TrophySearchQuery(
    Integer week,   // 주차
    String school,  // 학교
    String part     // 파트
) {
}
