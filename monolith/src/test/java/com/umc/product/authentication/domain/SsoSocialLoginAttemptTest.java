package com.umc.product.authentication.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.common.domain.enums.OAuthProvider;

@DisplayName("소셜 로그인 시도의 결속과 일회성")
class SsoSocialLoginAttemptTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");

    @Test
    @DisplayName("같은 제공자와 브라우저의 시도만 한 번 소비한다")
    void 정상_소비와_재사용_거절() {
        // given
        SsoSocialLoginAttempt attempt = attempt();
        // when
        attempt.consume(OAuthProvider.GOOGLE, "browser-hash", NOW);
        // then
        assertThat(attempt.getUsedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> attempt.consume(OAuthProvider.GOOGLE, "browser-hash", NOW))
            .isInstanceOf(AuthenticationDomainException.class);
    }

    @Test
    @DisplayName("다른 제공자 callback은 소비하지 않는다")
    void 제공자_불일치() {
        SsoSocialLoginAttempt attempt = attempt();
        assertThatThrownBy(() -> attempt.consume(OAuthProvider.KAKAO, "browser-hash", NOW))
            .isInstanceOf(AuthenticationDomainException.class);
        assertThat(attempt.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("다른 브라우저에서 가져온 state는 소비하지 않는다")
    void 브라우저_불일치() {
        SsoSocialLoginAttempt attempt = attempt();
        assertThatThrownBy(() -> attempt.consume(OAuthProvider.GOOGLE, "other-browser", NOW))
            .isInstanceOf(AuthenticationDomainException.class);
        assertThat(attempt.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("만료 시각과 같은 시각부터 거절한다")
    void 만료_경계() {
        SsoSocialLoginAttempt attempt = attempt();
        assertThatThrownBy(() -> attempt.consume(OAuthProvider.GOOGLE, "browser-hash", NOW.plusSeconds(600)))
            .isInstanceOf(AuthenticationDomainException.class);
        assertThat(attempt.getUsedAt()).isNull();
    }

    private SsoSocialLoginAttempt attempt() {
        return SsoSocialLoginAttempt.create(OAuthProvider.GOOGLE, "state-hash", "browser-hash",
            "macos-app", "umc-macos://auth/callback", "mac-state", "challenge",
            "https://api.example.com/google/callback", "nonce", NOW.plusSeconds(600));
    }
}
