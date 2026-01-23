package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Objects;

/**
 * ID 토큰 기반 OAuth 로그인 Command
 * <p>
 * 모바일 클라이언트에서 직접 OAuth 로그인 후 받은 토큰으로 인증할 때 사용합니다. - Google: ID Token - Kakao: Access Token
 *
 * @param provider OAuth Provider
 * @param token    검증할 토큰 (Google: ID Token, Kakao: Access Token)
 */
public record AccessTokenLoginCommand(
        OAuthProvider provider,
        String token
) {
    public AccessTokenLoginCommand {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(token, "token must not be null");
    }
}
