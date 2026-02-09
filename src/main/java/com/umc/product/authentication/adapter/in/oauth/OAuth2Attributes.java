package com.umc.product.authentication.adapter.in.oauth;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 인증을 통해서 얻은 사용자 정보를 담아놓음.
 * <p>
 * JSON 파싱을 담당한다고 생각하면 됨.
 */
@Getter
@Builder
@Slf4j
public class OAuth2Attributes {
    private Map<String, Object> attributes;
    private String email;
    private OAuthProvider provider;
    private String providerId;

    public static OAuth2Attributes of(
        String registrationId,
        Map<String, Object> attributes
    ) {

        log.info("OAuth2Attributes - RegistrationID: {}", registrationId);
        log.info("OAuth2Attributes - attributes: {}", attributes);

        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "apple" -> ofApple(attributes);
            default -> throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_PROVIDER_NOT_FOUND);
        };
    }

    // 구글에서 제공하는 형식에 맞게 Attributes 파싱
    private static OAuth2Attributes ofGoogle(
        Map<String, Object> attributes
    ) {
        return OAuth2Attributes.builder()
            .provider(OAuthProvider.GOOGLE)
            .providerId((String) attributes.get("sub"))
            .email((String) attributes.get("email"))
            .attributes(attributes)
            .build();
    }

    // 카카오에서 제공하는 형식에 맞게 Attributes 파싱
    private static OAuth2Attributes ofKakao(
        Map<String, Object> attributes
    ) {
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
            .provider(OAuthProvider.KAKAO)
            .providerId(String.valueOf(attributes.get("id")))
            .email((String) kakaoAccount.get("email"))
            .attributes(attributes)
            .build();
    }


    // 애플에서 제공하는 방식에 맞게 파싱
    private static OAuth2Attributes ofApple(
        Map<String, Object> attributes
    ) {
        return OAuth2Attributes.builder()
            .provider(OAuthProvider.APPLE)
            .providerId((String) attributes.get("sub"))
            .email((String) attributes.get("email"))
            .attributes(attributes)
            .build();
    }
}
