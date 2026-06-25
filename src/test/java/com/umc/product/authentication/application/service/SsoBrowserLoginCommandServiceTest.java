package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authentication.application.event.SsoBrowserLoginCreatedEvent;
import com.umc.product.authentication.application.port.in.command.dto.LoginSsoBrowserByEmailCommand;
import com.umc.product.authentication.application.port.in.dto.SsoBrowserLoginInfo;
import com.umc.product.authentication.config.SsoProperties;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("SsoBrowserLoginCommandService")
class SsoBrowserLoginCommandServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String EMAIL = "alice@example.com";
    private static final String RAW_PASSWORD = "Strong-Pw-2026";
    private static final String LOGIN_TOKEN = "sso-login-token";

    @Mock
    SsoCredentialVerifier credentialVerifier;

    @Mock
    SsoLoginTokenProvider tokenProvider;

    @Mock
    DomainEventPublisher eventPublisher;

    SsoBrowserLoginCommandService service;

    @BeforeEach
    void setUp() {
        SsoProperties properties = new SsoProperties(
            URI.create("https://api.university.neordinary.com"),
            Duration.ofMinutes(3),
            Duration.ofHours(12),
            new SsoProperties.Cookie("UMC_SSO_LOGIN", "", true, "Lax"),
            Map.of()
        );
        service = new SsoBrowserLoginCommandService(
            credentialVerifier,
            tokenProvider,
            properties,
            eventPublisher
        );
    }

    @Test
    @DisplayName("email/password 검증 후 SSO 브라우저 로그인 토큰을 발급하고 생성 이벤트를 발행한다")
    void 이메일_로그인_성공_토큰_발급_이벤트_발행() {
        // given
        given(credentialVerifier.verifyEmailPassword(EMAIL, RAW_PASSWORD)).willReturn(MEMBER_ID);
        given(tokenProvider.createLoginToken(any(), any(), any())).willReturn(LOGIN_TOKEN);

        // when
        SsoBrowserLoginInfo result = service.loginByEmail(
            LoginSsoBrowserByEmailCommand.of(EMAIL, RAW_PASSWORD)
        );

        // then
        assertThat(result.memberId()).isEqualTo(MEMBER_ID);
        assertThat(result.loginToken()).isEqualTo(LOGIN_TOKEN);
        assertThat(result.expiresAt()).isAfter(Instant.now().plus(Duration.ofHours(11)));

        ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
        then(tokenProvider).should().createLoginToken(eq(MEMBER_ID), eq("email"), expiresAtCaptor.capture());
        assertThat(expiresAtCaptor.getValue()).isAfter(Instant.now().plus(Duration.ofHours(11)));

        ArgumentCaptor<SsoBrowserLoginCreatedEvent> eventCaptor =
            ArgumentCaptor.forClass(SsoBrowserLoginCreatedEvent.class);
        then(eventPublisher).should().publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().memberId()).isEqualTo(MEMBER_ID);
        assertThat(eventCaptor.getValue().authenticationMethod()).isEqualTo("email");
        assertThat(eventCaptor.getValue().eventType()).isEqualTo("authentication.sso.browser-login.created");
    }
}
