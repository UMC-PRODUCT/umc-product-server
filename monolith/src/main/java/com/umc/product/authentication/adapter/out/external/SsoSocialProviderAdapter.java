package com.umc.product.authentication.adapter.out.external;

import java.net.URI;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.umc.product.authentication.application.port.out.AppleAuthorizationCodeResult;
import com.umc.product.authentication.application.port.out.SsoSocialProviderPort;
import com.umc.product.authentication.config.SsoSocialProperties;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.common.domain.enums.OAuthProvider;

import lombok.RequiredArgsConstructor;

/**
 * application의 공통 로그인 요청을 Apple·Google·Kakao별 인가 URL과 코드 교환 호출로 변환한다.
 * 시작 URL과 코드 교환은 같은 서버 callback/client 설정을 사용하며, 앱 복귀 주소는 여기서 다루지 않는다.
 */
@Component
@RequiredArgsConstructor
public class SsoSocialProviderAdapter implements SsoSocialProviderPort {
    private final SsoSocialProperties properties;
    private final KakaoOAuthProperties kakaoProperties;
    private final AppleOAuthProperties appleProperties;
    private final GoogleTokenVerifier google;
    private final KakaoTokenVerifier kakao;
    private final AppleTokenVerifier apple;

    @Override
    public Authorization prepare(OAuthProvider provider, String state, String nonce) {
        SsoSocialProperties.Provider config = configuration(provider);
        String clientId = clientId(provider, config);
        String endpoint = switch (provider) {
            case GOOGLE -> "https://accounts.google.com/o/oauth2/v2/auth";
            case KAKAO -> "https://kauth.kakao.com/oauth/authorize";
            case APPLE -> "https://appleid.apple.com/auth/authorize";
        };
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(endpoint)
            .queryParam("client_id", clientId).queryParam("redirect_uri", config.callbackUri())
            .queryParam("response_type", "code").queryParam("state", state);
        // nonce는 ID Token을 이번 시도와 묶는다. Kakao는 콘솔과 설정에서 OIDC를 사용할 때만 요청한다.
        // Apple의 name/email scope는 form_post 응답을 요구하므로 서버 callback도 POST로 구성한다.
        switch (provider) {
            case GOOGLE -> uri.queryParam("scope", "openid email profile").queryParam("nonce", nonce);
            case APPLE -> uri.queryParam("scope", "name email").queryParam("response_mode", "form_post")
                .queryParam("nonce", nonce);
            case KAKAO -> {
                if (config.oidc()) {
                    uri.queryParam("scope", "openid").queryParam("nonce", nonce);
                }
            }
            default -> throw unavailable();
        }
        return new Authorization(uri.build().encode().toUriString(), config.callbackUri().toString());
    }

    @Override
    public Identity exchange(OAuthProvider provider, String code, String callbackUri, String nonce) {
        SsoSocialProperties.Provider config = configuration(provider);
        // 시작 뒤 환경 설정이 바뀌어도 다른 callback으로 코드를 교환하지 않는다.
        if (!config.callbackUri().toString().equals(callbackUri)) {
            throw unavailable();
        }
        // 세 제공자의 교환 차이는 기존 verifier에 위임한다. 반환값은 서명/신원 검증을 마친 회원 식별 정보다.
        // Apple refresh token과 client ID는 추후 연결 해제에 필요하므로 함께 기존 회원 저장 경로에 전달한다.
        return switch (provider) {
            case GOOGLE -> new Identity(google.verifyAuthorizationCode(code, callbackUri,
                config.clientId(), config.clientSecret(), nonce), null, config.clientId());
            case KAKAO -> new Identity(kakao.verifyAuthorizationCode(code, callbackUri,
                config.oidc() ? nonce : null), null, kakaoProperties.clientId());
            case APPLE -> {
                // Apple 인증은 서버 web Services ID로 검증한다. 최종 UMC 토큰은 macos-app 기준 MACOS로 발급된다.
                AppleAuthorizationCodeResult result = apple.verifyAuthorizationCode(code, ClientType.WEB,
                    callbackUri, nonce);
                yield new Identity(result.attrs(), result.refreshToken(), result.clientId());
            }
        };
    }

    /** 비활성화·누락된 비밀값·잘못된 callback 설정은 외부 로그인 시작 전에 거절한다. */
    private SsoSocialProperties.Provider configuration(OAuthProvider provider) {
        SsoSocialProperties.Provider config = properties.providers().get(provider.name().toLowerCase(Locale.ROOT));
        if (config == null || !config.enabled() || !validCallback(provider, config.callbackUri())
            || !StringUtils.hasText(clientId(provider, config))) {
            throw unavailable();
        }
        if (provider == OAuthProvider.GOOGLE && !StringUtils.hasText(config.clientSecret())) {
            throw unavailable();
        }
        if (provider == OAuthProvider.KAKAO && !kakaoProperties.isAllowedRedirectUri(config.callbackUri().toString())) {
            throw unavailable();
        }
        if (provider == OAuthProvider.APPLE && (!StringUtils.hasText(appleProperties.privateKey())
            || !StringUtils.hasText(appleProperties.keyId()) || !StringUtils.hasText(appleProperties.teamId()))) {
            throw unavailable();
        }
        return config;
    }

    private String clientId(OAuthProvider provider, SsoSocialProperties.Provider config) {
        return switch (provider) {
            case GOOGLE -> config.clientId();
            case KAKAO -> kakaoProperties.clientId();
            case APPLE -> appleProperties.webClientId();
        };
    }

    /** 제공자에게 등록할 서버 주소만 허용한다. query/fragment/userinfo가 섞인 주소는 받지 않는다. */
    private boolean validCallback(OAuthProvider provider, URI uri) {
        return uri != null && "https".equals(uri.getScheme()) && uri.getHost() != null
            && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null
            && ("/api/v1/auth/sso/social/" + provider.name().toLowerCase(Locale.ROOT) + "/callback")
                .equals(uri.getPath());
    }

    private AuthenticationDomainException unavailable() {
        return new AuthenticationDomainException(AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE);
    }
}
