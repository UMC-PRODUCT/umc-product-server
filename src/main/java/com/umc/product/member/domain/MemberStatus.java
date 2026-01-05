package com.umc.product.member.domain;

public enum MemberStatus {
    PENDING,    // OAuth 로그인 시도 했던 사람
    ACTIVE,     // 유효한 회원
    INACTIVE,   // 휴면 계정
    WITHDRAWN   // 탈퇴 계정
}
