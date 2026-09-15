package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * ID 토큰 로그인 결과
 *
 * @param isExistingMember 기존 회원 여부
 * @param memberId         기존 회원인 경우 회원 ID (신규 회원이면 null)
 * @param provider         OAuth Provider
 * @param providerId       OAuth Provider의 사용자 고유 ID
 * @param email            이메일
 * @param name             이름
 * @param nickname         닉네임
 */
public record OAuthTokenLoginResult(
    boolean isExistingMember,
    Long memberId,
    OAuthProvider provider,
    String providerId,
    String email
) {
    /**
     * 기존 회원 로그인 성공
     */
    public static OAuthTokenLoginResult existingMember(
        Long memberId,
        OAuthProvider provider,
        String providerId,
        String email
    ) {
        return new OAuthTokenLoginResult(true, memberId, provider, providerId, email);
    }

    /**
     * 신규 회원 - 회원가입 필요
     */
    public static OAuthTokenLoginResult newMember(
        OAuthProvider provider,
        String providerId,
        String email
    ) {
        return new OAuthTokenLoginResult(false, null, provider, providerId, email);
    }
}
