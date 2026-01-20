package com.umc.product.authentication.adapter.out.external;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.out.VerifyIdTokenPort;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ID 토큰 검증 통합 Adapter
 * <p>
 * Provider별 검증 로직을 위임하는 Facade 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdTokenVerificationAdapter implements VerifyIdTokenPort {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final KakaoTokenVerifier kakaoTokenVerifier;

    @Override
    public OAuth2Attributes verify(OAuthProvider provider, String token) {
        log.info("OAuth 토큰 검증 시작: provider={}", provider);

        return switch (provider) {
            case GOOGLE -> googleIdTokenVerifier.verify(token);
            case KAKAO -> kakaoTokenVerifier.verify(token);
            case APPLE -> throw new AuthenticationDomainException(
                    AuthenticationErrorCode.OAUTH_PROVIDER_NOT_FOUND
            ); // TODO: Apple 구현 필요
        };
    }
}
