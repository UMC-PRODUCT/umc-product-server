package com.umc.product.authentication.domain;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Map;

/**
 * OAuth 인증을 통해 얻은 사용자 정보를 담는 도메인 VO.
 * <p>
 * Provider 별 응답 형식을 도메인 표현으로 정규화한다. 외부 토큰 검증 어댑터들의
 * 검증 결과와 application 계층의 로그인 처리 사이를 가로지르는 공통 자료구조다.
 */
public record OAuthAttributes(
    OAuthProvider provider,
    String providerId,
    String email
) {
    public static OAuthAttributes of(
        String registrationId,
        Map<String, Object> attributes
    ) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "apple" -> ofApple(attributes);
            default -> throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_PROVIDER_NOT_FOUND);
        };
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
            OAuthProvider.GOOGLE,
            (String) attributes.get("sub"),
            (String) attributes.get("email")
        );
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        return new OAuthAttributes(
            OAuthProvider.KAKAO,
            String.valueOf(attributes.get("id")),
            (String) kakaoAccount.get("email")
        );
    }

    private static OAuthAttributes ofApple(Map<String, Object> attributes) {
        return new OAuthAttributes(
            OAuthProvider.APPLE,
            (String) attributes.get("sub"),
            (String) attributes.get("email")
        );
    }
}
