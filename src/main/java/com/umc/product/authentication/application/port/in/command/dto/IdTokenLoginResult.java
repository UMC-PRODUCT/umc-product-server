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
public record IdTokenLoginResult(
        boolean isExistingMember,
        Long memberId,
        OAuthProvider provider,
        String providerId,
        String email,
        String name,
        String nickname
) {
    /**
     * 기존 회원 로그인 성공
     */
    public static IdTokenLoginResult existingMember(
            Long memberId,
            OAuthProvider provider,
            String providerId,
            String email
    ) {
        return new IdTokenLoginResult(true, memberId, provider, providerId, email, null, null);
    }

    /**
     * 신규 회원 - 회원가입 필요
     */
    public static IdTokenLoginResult newMember(
            OAuthProvider provider,
            String providerId,
            String email,
            String name,
            String nickname
    ) {
        return new IdTokenLoginResult(false, null, provider, providerId, email, name, nickname);
    }
}
