package com.umc.product.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;

/**
 * JwtTokenProvider 의 emailVerificationToken purpose claim 검증 단위 테스트.
 * <p>
 * 회원가입(REGISTER) 흐름에서 발급된 토큰이 비밀번호 초기화(PASSWORD_RESET) 나 이메일 변경(CHANGE_EMAIL) 에 재사용되지
 * 않도록, 발급 시 purpose 를 claim 으로 심고 파싱 시 expectedPurpose 와 일치해야만
 * 통과시키는 동작을 검증한다.
 */
class JwtTokenProviderEmailVerificationTest {

    private static final String EMAIL = "alice@example.com";
    private static final String ACCESS_TOKEN_SECRET = "test-access-token-secret-must-be-long-enough-for-hmac-sha256";
    private static final String REFRESH_TOKEN_SECRET = "test-refresh-token-secret-must-be-long-enough-for-hmac-sha256";
    private static final String OAUTH_VERIFICATION_TOKEN_SECRET =
        "test-oauth-verification-token-secret-must-be-long-enough-for-hmac-sha256";
    private static final String EMAIL_VERIFICATION_TOKEN_SECRET =
        "test-email-verification-token-secret-must-be-long-enough-for-hmac-sha256";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
            ACCESS_TOKEN_SECRET,
            REFRESH_TOKEN_SECRET,
            OAUTH_VERIFICATION_TOKEN_SECRET,
            EMAIL_VERIFICATION_TOKEN_SECRET,
            3600L,
            3600L,
            600L
        );
    }

    @Test
    @DisplayName("REGISTER 로 발급한 토큰은 REGISTER 로 파싱 시 이메일을 반환한다")
    void purpose_일치_REGISTER_정상_파싱() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.REGISTER);

        // when
        String parsedEmail = provider.parseEmailVerificationToken(token, EmailVerificationPurpose.REGISTER);

        // then
        assertThat(parsedEmail).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("PASSWORD_RESET 로 발급한 토큰은 PASSWORD_RESET 로 파싱 시 이메일을 반환한다")
    void purpose_일치_PASSWORD_RESET_정상_파싱() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.PASSWORD_RESET);

        // when
        String parsedEmail = provider.parseEmailVerificationToken(token, EmailVerificationPurpose.PASSWORD_RESET);

        // then
        assertThat(parsedEmail).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("CHANGE_EMAIL 로 발급한 토큰은 CHANGE_EMAIL 로 파싱 시 이메일을 반환한다")
    void purposeMatchesChangeEmail() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.CHANGE_EMAIL);

        // when
        String parsedEmail = provider.parseEmailVerificationToken(token, EmailVerificationPurpose.CHANGE_EMAIL);

        // then
        assertThat(parsedEmail).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("REGISTER 토큰을 PASSWORD_RESET 로 파싱하면 INVALID_EMAIL_VERIFICATION 예외를 던진다")
    void cross_purpose_REGISTER_to_RESET_거부() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.REGISTER);

        // when / then
        assertThatThrownBy(() ->
            provider.parseEmailVerificationToken(token, EmailVerificationPurpose.PASSWORD_RESET))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }

    @Test
    @DisplayName("PASSWORD_RESET 토큰을 REGISTER 로 파싱하면 INVALID_EMAIL_VERIFICATION 예외를 던진다")
    void cross_purpose_RESET_to_REGISTER_거부() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.PASSWORD_RESET);

        // when / then
        assertThatThrownBy(() ->
            provider.parseEmailVerificationToken(token, EmailVerificationPurpose.REGISTER))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }

    @Test
    @DisplayName("REGISTER 토큰을 CHANGE_EMAIL 로 파싱하면 INVALID_EMAIL_VERIFICATION 예외를 던진다")
    void registerTokenRejectedForChangeEmail() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.REGISTER);

        // when / then
        assertThatThrownBy(() ->
            provider.parseEmailVerificationToken(token, EmailVerificationPurpose.CHANGE_EMAIL))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }

    @Test
    @DisplayName("PASSWORD_RESET 토큰을 CHANGE_EMAIL 로 파싱하면 INVALID_EMAIL_VERIFICATION 예외를 던진다")
    void passwordResetTokenRejectedForChangeEmail() {
        // given
        String token = provider.createEmailVerificationToken(EMAIL, EmailVerificationPurpose.PASSWORD_RESET);

        // when / then
        assertThatThrownBy(() ->
            provider.parseEmailVerificationToken(token, EmailVerificationPurpose.CHANGE_EMAIL))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
    }
}
