package com.umc.product.authentication.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import com.umc.product.authentication.config.SsoSocialProperties;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.common.domain.enums.OAuthProvider;

@DisplayName("소셜 제공자 시작 설정과 callback 고정")
class SsoSocialProviderAdapterTest {
    @Test
    @DisplayName("Google web client는 고정 HTTPS callback과 nonce로 시작한다")
    void google_시작() {
        var adapter = adapter("google", true, callback("google"));
        var result = adapter.prepare(OAuthProvider.GOOGLE, "provider-state", "nonce");
        var uri = UriComponentsBuilder.fromUriString(result.uri()).build();
        assertThat(uri.getHost()).isEqualTo("accounts.google.com");
        assertThat(uri.getQueryParams().getFirst("client_id")).isEqualTo("web-client");
        assertThat(uri.getQueryParams().getFirst("nonce")).isEqualTo("nonce");
        assertThat(result.callbackUri()).isEqualTo(callback("google"));
        assertThat(result.uri()).doesNotContain("web-secret");
    }

    @Test
    @DisplayName("Apple은 Services ID와 form_post 응답 모드로 시작한다")
    void apple_시작() {
        var result = adapter("apple", true, callback("apple")).prepare(OAuthProvider.APPLE, "state", "nonce");
        assertThat(result.uri()).contains("client_id=apple-services-id", "response_mode=form_post")
            .doesNotContain("apple-bundle-id");
    }

    @Test
    @DisplayName("꺼진 제공자·HTTP·다른 callback 경로는 시작하지 않는다")
    void 설정_거절() {
        assertThatThrownBy(() -> adapter("google", false, callback("google"))
            .prepare(OAuthProvider.GOOGLE, "state", "nonce")).isInstanceOf(AuthenticationDomainException.class);
        assertThatThrownBy(() -> adapter("google", true, callback("google").replace("https:", "http:"))
            .prepare(OAuthProvider.GOOGLE, "state", "nonce")).isInstanceOf(AuthenticationDomainException.class);
        assertThatThrownBy(() -> adapter("google", true, callback("kakao"))
            .prepare(OAuthProvider.GOOGLE, "state", "nonce")).isInstanceOf(AuthenticationDomainException.class);
    }

    private SsoSocialProviderAdapter adapter(String provider, boolean enabled, String callback) {
        return new SsoSocialProviderAdapter(new SsoSocialProperties(Map.of(provider,
            new SsoSocialProperties.Provider(enabled, URI.create(callback), "web-client", "web-secret", true))),
            new KakaoOAuthProperties("kakao-client", "", "", List.of(callback("kakao")), null),
            new AppleOAuthProperties("apple-bundle-id", "apple-services-id", "team", "key", "private-key", null),
            null, null, null);
    }

    private String callback(String provider) {
        return "https://api.example.com/api/v1/auth/sso/social/" + provider + "/callback";
    }
}
