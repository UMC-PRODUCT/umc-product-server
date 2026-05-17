package com.umc.product.authentication.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CredentialPolicy 단위 테스트. ADR-017 흐름.
 * <p>
 * 형식/길이/카테고리/공백 정책에 대한 SSOT 의 동작을 검증한다.
 */
class CredentialPolicyTest {

    @Nested
    @DisplayName("validateEmail")
    class ValidateEmail {

        @ParameterizedTest
        @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.kr",
            "user_name@example.com",
            "user-name@sub.example.com",
            "u@a.io"
        })
        @DisplayName("정상 형식의 이메일은 통과한다")
        void 정상_형식_통과(String email) {
            assertThatCode(() -> CredentialPolicy.validateEmail(email))
                .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user",                     // @ 없음
            "user@",                    // 도메인 없음
            "@example.com",             // 로컬 파트 없음
            "user@example",             // TLD 없음
            "user@.com",                // 도메인 시작이 점
            "user name@example.com",    // 공백 포함
            "user@exa mple.com",        // 도메인에 공백
            "user@@example.com",        // @ 중복
            "한글@example.com"          // 비ASCII 로컬 파트
        })
        @DisplayName("형식이 어긋나면 INVALID_EMAIL_FORMAT 예외를 던진다")
        void 잘못된_형식이면_예외(String email) {
            assertThatThrownBy(() -> CredentialPolicy.validateEmail(email))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_FORMAT);
        }

        @Test
        @DisplayName("null 이메일은 예외를 던진다")
        void null이면_예외() {
            assertThatThrownBy(() -> CredentialPolicy.validateEmail(null))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_FORMAT);
        }

        @Test
        @DisplayName("100 자를 초과하는 이메일은 예외를 던진다")
        void 길이_초과면_예외() {
            String tooLong = "a".repeat(95) + "@a.io"; // 100자 초과
            assertThatThrownBy(() -> CredentialPolicy.validateEmail(tooLong))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    @Nested
    @DisplayName("validatePassword")
    class ValidatePassword {

        @ParameterizedTest
        @ValueSource(strings = {
            "passw0rd",                    // 영문+숫자, 8자 (최소)
            "Pa$$word",                    // 영문+특수문자
            "12345678!",                   // 숫자+특수문자
            "Strong-Password-2026!",       // 영문+숫자+특수문자
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@" // 64자 경계
        })
        @DisplayName("정상 정책의 비밀번호는 통과한다")
        void 정상_정책_통과(String password) {
            assertThatCode(() -> CredentialPolicy.validatePassword(password))
                .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "passwd1",     // 7자 (최소 미달)
            "abcdefg"      // 7자 + 단일 카테고리
        })
        @DisplayName("길이가 8자 미만이면 PASSWORD_POLICY_VIOLATION 예외")
        void 길이_부족이면_예외(String password) {
            assertThatThrownBy(() -> CredentialPolicy.validatePassword(password))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        @Test
        @DisplayName("64자 초과 비밀번호는 예외를 던진다")
        void 길이_초과면_예외() {
            String tooLong = "a".repeat(65) + "1"; // 66자
            assertThatThrownBy(() -> CredentialPolicy.validatePassword(tooLong))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "abcdefgh",     // 영문만
            "12345678",     // 숫자만
            "!@#$%^&*"      // 특수문자만
        })
        @DisplayName("단일 카테고리 비밀번호는 거부한다")
        void 단일_카테고리면_예외(String password) {
            assertThatThrownBy(() -> CredentialPolicy.validatePassword(password))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "passwd 12",   // 공백 포함
            "passwd\t12",  // 탭 포함
            "passwd\n12"   // 개행 포함
        })
        @DisplayName("공백/제어문자가 포함된 비밀번호는 거부한다")
        void 공백_혹은_제어문자면_예외(String password) {
            assertThatThrownBy(() -> CredentialPolicy.validatePassword(password))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        @Test
        @DisplayName("null 비밀번호는 예외를 던진다")
        void null이면_예외() {
            assertThatThrownBy(() -> CredentialPolicy.validatePassword(null))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.PASSWORD_POLICY_VIOLATION);
        }
    }
}
