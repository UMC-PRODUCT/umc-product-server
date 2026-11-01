package com.umc.product.authentication.config;

import java.net.URI;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서버 소셜 로그인용 설정이다. 기존 모바일 SDK 로그인 설정과 별도로 제공자별 활성화를 제어한다.
 * Kakao와 Apple의 공통 자격증명은 기존 Properties에서 재사용하고, Google web client는 여기서 설정한다.
 */
@ConfigurationProperties(prefix = "app.sso.social")
public record SsoSocialProperties(Map<String, Provider> providers) {
    public SsoSocialProperties {
        providers = providers == null ? Map.of() : Map.copyOf(providers);
    }

    /** callbackUri는 서버 HTTPS 주소다. clientSecret이 포함되어 있으므로 record 기본 toString을 사용하지 않는다. */
    public record Provider(boolean enabled, URI callbackUri, String clientId, String clientSecret, boolean oidc) {
        @Override
        public String toString() {
            return "SocialProviderConfiguration[redacted]";
        }
    }
}
