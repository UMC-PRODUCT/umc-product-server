package com.umc.product.feedback.domain.enums;

public enum UserFeedbackTargetType {
    CHALLENGER,           // 신규·기존 챌린저가 함께 사용하는 공통 템플릿 대상
    NEW_CHALLENGER,       // 이번 기수가 첫 활동인 챌린저
    EXPERIENCED_CHALLENGER, // 이전 기수 경력이 있는 챌린저 (PM 포함)
    ADMIN,                // 현재 기수 중앙 운영진 또는 SUPER_ADMIN 대상
}
