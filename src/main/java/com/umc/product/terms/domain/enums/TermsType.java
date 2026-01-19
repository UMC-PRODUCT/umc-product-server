package com.umc.product.terms.domain.enums;

public enum TermsType {
    SERVICE("서비스 이용약관"),
    PRIVACY("개인정보 처리방침"),
    MARKETING("마케팅 정보 수신 동의"),
    LOCATION("위치기반 서비스 이용약관");
    private final String description;

    TermsType(String description) {
        this.description = description;
    }
}
