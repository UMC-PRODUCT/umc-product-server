package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * Mac의 시작 요청이다. state는 Mac이 생성한 값, browserBinding은 서버가 쿠키로 설정한 확인값이다.
 * codeChallenge만 서버로 보내고 원본 verifier는 Mac에 보관한다. 민감값의 자동 로깅을 막기 위해 toString을 숨긴다.
 */
public record StartSsoSocialLoginCommand(OAuthProvider provider, String clientId, String redirectUri,
    String responseType, String state, String codeChallenge, String codeChallengeMethod, String browserBinding) {
    public static StartSsoSocialLoginCommand of(OAuthProvider provider, String clientId, String redirectUri,
        String responseType, String state, String challenge, String method, String binding) {
        return new StartSsoSocialLoginCommand(provider, clientId, redirectUri, responseType,
            state, challenge, method, binding);
    }

    @Override
    public String toString() {
        return "StartSsoSocialLoginCommand[redacted]";
    }
}
