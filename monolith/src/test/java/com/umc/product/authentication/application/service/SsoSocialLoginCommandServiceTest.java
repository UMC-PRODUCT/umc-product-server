package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authentication.application.port.in.command.AuthorizeSsoUseCase;
import com.umc.product.authentication.application.port.in.command.ManageSsoBrowserLoginUseCase;
import com.umc.product.authentication.application.port.in.command.dto.CompleteSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.SsoAuthorizationRedirectInfo;
import com.umc.product.authentication.application.port.in.command.dto.StartSsoSocialLoginCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserLoginInfo;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserOAuthLoginResult;
import com.umc.product.authentication.application.port.out.LoadSsoClientPort;
import com.umc.product.authentication.application.port.out.SsoSocialLoginAttemptPort;
import com.umc.product.authentication.application.port.out.SsoSocialProviderPort;
import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.authentication.domain.SsoClient;
import com.umc.product.authentication.domain.SsoSocialLoginAttempt;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.client.ClientEnvironment;
import com.umc.product.global.client.ClientServiceType;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mac 소셜 SSO 시작과 완료")
class SsoSocialLoginCommandServiceTest {
    @Mock LoadSsoClientPort clients;
    @Mock SsoSocialLoginAttemptPort attempts;
    @Mock SsoSocialLoginAttemptService consumer;
    @Mock SsoSocialProviderPort provider;
    @Mock ManageSsoBrowserLoginUseCase browser;
    @Mock AuthorizeSsoUseCase authorize;
    private SsoSocialLoginCommandService service;

    @BeforeEach
    void setUp() {
        service = new SsoSocialLoginCommandService(clients, attempts, consumer, provider, browser, authorize,
            new SecureTokenGenerator(), new PkceVerifier(), java.time.Clock.systemUTC());
    }

    @Test
    @DisplayName("등록되지 않은 앱 callback이면 외부 로그인과 시도 저장 전에 거절한다")
    void 잘못된_앱_uri() {
        given(clients.getByClientId("macos-app")).willReturn(client());
        assertThatThrownBy(() -> service.start(new StartSsoSocialLoginCommand(OAuthProvider.GOOGLE,
            "macos-app", "umc-evil://callback", "code", "mac-state", "A".repeat(43), "S256", null)))
            .isInstanceOf(AuthenticationDomainException.class);
        verifyNoInteractions(provider, attempts);
    }

    @Test
    @DisplayName("연결 회원은 기존 authorize에서 발급한 UMC 코드로 복귀한다")
    void 기존_회원_성공() {
        given(consumer.consume(OAuthProvider.GOOGLE, "state", "binding")).willReturn(attempt());
        OAuthAttributes attrs = OAuthAttributes.of("google", Map.of("sub", "provider-id", "email", "a@example.com"));
        given(provider.exchange(OAuthProvider.GOOGLE, "provider-code", "https://api.example.com/callback", "nonce"))
            .willReturn(new SsoSocialProviderPort.Identity(attrs, null, "server-client"));
        given(browser.loginByVerifiedOAuth(eq(attrs), any(), eq("server-client")))
            .willReturn(SsoBrowserOAuthLoginResult.loginSuccess(OAuthProvider.GOOGLE,
                SsoBrowserLoginInfo.of(1L, "internal-login-token", Instant.now().plusSeconds(600))));
        given(authorize.authorize(any())).willReturn(new SsoAuthorizationRedirectInfo(
            "umc-macos://auth/callback?code=umc-code&state=mac-state"));
        assertThat(service.complete(command(null))).contains("code=umc-code").doesNotContain("provider-code");
    }

    @Test
    @DisplayName("미연결 계정은 가입 필요로 복귀하고 UMC 코드를 발급하지 않는다")
    void 미연결_회원() {
        given(consumer.consume(OAuthProvider.GOOGLE, "state", "binding")).willReturn(attempt());
        OAuthAttributes attrs = OAuthAttributes.of("google", Map.of("sub", "provider-id", "email", "a@example.com"));
        given(provider.exchange(any(), any(), any(), any()))
            .willReturn(new SsoSocialProviderPort.Identity(attrs, null, "server-client"));
        given(browser.loginByVerifiedOAuth(eq(attrs), any(), eq("server-client")))
            .willReturn(SsoBrowserOAuthLoginResult.registerRequired(OAuthProvider.GOOGLE, null));
        assertThat(service.complete(command(null)))
            .isEqualTo("umc-macos://auth/callback?error=registration_required&state=mac%20state%26x");
        verifyNoInteractions(authorize);
    }

    @Test
    @DisplayName("취소 응답은 검증된 시도만 소비하고 제공자 코드를 교환하지 않는다")
    void 사용자_취소() {
        given(consumer.consume(OAuthProvider.GOOGLE, "state", "binding")).willReturn(attempt());
        assertThat(service.complete(command("access_denied"))).contains("error=access_denied");
        verifyNoInteractions(provider, browser, authorize);
    }

    @Test
    @DisplayName("브라우저 결속 실패는 앱 redirect로 바꾸지 않는다")
    void 검증되지_않은_시도() {
        given(consumer.consume(OAuthProvider.GOOGLE, "state", "binding"))
            .willThrow(new AuthenticationDomainException(AuthenticationErrorCode.INVALID_SSO_SOCIAL_ATTEMPT));
        assertThatThrownBy(() -> service.complete(command(null))).isInstanceOf(AuthenticationDomainException.class);
        verifyNoInteractions(provider, browser, authorize);
    }

    @Test
    @DisplayName("제공자 장애는 검증된 앱 주소로 일시 장애를 알려준다")
    void 제공자_일시_장애() {
        given(consumer.consume(OAuthProvider.GOOGLE, "state", "binding")).willReturn(attempt());
        given(provider.exchange(any(), any(), any(), any()))
            .willThrow(new AuthenticationDomainException(AuthenticationErrorCode.SSO_SOCIAL_PROVIDER_UNAVAILABLE));
        assertThat(service.complete(command(null)))
            .isEqualTo("umc-macos://auth/callback?error=temporarily_unavailable&state=mac%20state%26x");
        verifyNoInteractions(browser, authorize);
    }

    private CompleteSsoSocialLoginCommand command(String error) {
        return new CompleteSsoSocialLoginCommand(OAuthProvider.GOOGLE, error == null ? "provider-code" : null,
            "state", error, "binding");
    }

    private SsoSocialLoginAttempt attempt() {
        return SsoSocialLoginAttempt.create(OAuthProvider.GOOGLE, "state-hash", "binding-hash", "macos-app",
            "umc-macos://auth/callback", "mac state&x", "A".repeat(43),
            "https://api.example.com/callback", "nonce", Instant.now().plusSeconds(600));
    }

    private SsoClient client() {
        return SsoClient.of("macos-app", "Mac", ClientServiceType.MACOS_APP, ClientEnvironment.PROD, true,
            Duration.ofHours(1), List.of("umc-macos://auth/callback"), List.of());
    }
}
