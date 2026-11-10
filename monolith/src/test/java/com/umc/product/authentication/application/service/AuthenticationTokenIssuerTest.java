package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authentication.application.port.in.command.dto.NewTokens;
import com.umc.product.authentication.application.port.out.SaveRefreshTokenPort;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.global.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthenticationTokenIssuerTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    private JwtTokenProvider jwtTokenProvider;
    private AuthenticationTokenIssuer issuer;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            "test-access-token-secret-must-be-long-enough-for-hmac-sha256",
            "test-refresh-token-secret-must-be-long-enough-for-hmac-sha256",
            "test-oauth-verification-token-secret-must-be-long-enough-for-hmac-sha256",
            "test-email-verification-token-secret-must-be-long-enough-for-hmac-sha256",
            "test-sso-login-token-secret-must-be-long-enough-for-hmac-sha256",
            3600L,
            3600L,
            600L
        );
        issuer = new AuthenticationTokenIssuer(jwtTokenProvider, saveRefreshTokenPort);
    }

    @Test
    @DisplayName("로그인 때 받은 clientType을 RefreshToken에도 담아 재발급 때 다시 읽을 수 있다")
    void refresh_token에_client_type_저장() {
        // when
        NewTokens tokens = issuer.issue(MEMBER_ID, ClientType.IOS);

        // then
        assertThat(jwtTokenProvider.parseRefreshToken(tokens.refreshToken()).clientType()).isEqualTo(ClientType.IOS);
    }
}
