package com.umc.product.authentication.adapter.out.external;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.out.AppleAuthorizationCodeResult;
import com.umc.product.authentication.application.port.out.RevokeOAuthTokenPort;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OAuth Token 검증
 * <p>
 * Provider별 검증 로직을 위임하는 Facade 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthTokenVerificationAdapter implements VerifyOAuthTokenPort, RevokeOAuthTokenPort {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final KakaoTokenVerifier kakaoTokenVerifier;
    private final AppleTokenVerifier appleTokenVerifier;

    @Override
    public OAuth2Attributes verify(OAuthProvider provider, String token) {
        log.info("OAuth 토큰 검증 시작: provider={}", provider);

        return switch (provider) {
            case GOOGLE -> googleTokenVerifier.verifyAccessToken(token);
            case KAKAO -> kakaoTokenVerifier.verifyAccessToken(token);
            case APPLE -> appleTokenVerifier.verifyIdToken(token);
        };
    }

    @Override
    public AppleAuthorizationCodeResult verifyAppleAuthorizationCode(String authorizationCode) {
        log.info("Apple Authorization Code 교환 시작");
        return appleTokenVerifier.verifyAuthorizationCode(authorizationCode);
    }

    @Override
    public void revokeAppleToken(String refreshToken) {
        log.info("Apple token revoke 시작");
        appleTokenVerifier.revokeToken(refreshToken);
    }

    @Override
    public void revokeKakaoToken(String accessToken) {
        log.info("Kakao 사용자 연결 끊기 시작");
        kakaoTokenVerifier.unlinkUser(accessToken);
    }

    @Override
    public void revokeGoogleToken(String token) {
        log.info("Google token revoke 시작");
        googleTokenVerifier.revokeToken(token);
    }
}
