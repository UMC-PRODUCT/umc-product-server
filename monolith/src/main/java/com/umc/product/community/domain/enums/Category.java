package com.umc.product.community.domain.enums;

public enum Category {
    LIGHTNING,   // 번개
    HABIT,       // 취미
    QUESTION,    // 질문
    INFORMATION, // 정보
    FREE,         // 자유
    ;

    public boolean isLightning() {
        return this == LIGHTNING;
    }
}
