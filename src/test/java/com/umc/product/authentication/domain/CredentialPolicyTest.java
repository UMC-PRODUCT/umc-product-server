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
 * CredentialPolicy 단위 테스트.
 * <p>
 * 형식/길이/카테고리/공백 정책에 대한 SSOT 의 동작을 검증한다.
 */
class CredentialPolicyTest {

    @Nested
    @DisplayName("validateLoginId")
    class ValidateLoginId {

        @ParameterizedTest
        @ValueSource(strings = {
            "alice",            // 5자 영문
            "alice01",          // 영문 + 숫자
            "user.name",        // 점 허용
            "user_name",        // 밑줄 허용
            "user-name",        // 하이픈 허용
            "ABCDE12345abcde12345" // 정확히 20자 경계값
        })
        @DisplayName("정상 형식의 로그인 ID 는 통과한다")
        void 정상_형식_통과(String loginId) {
            assertThatCode(() -> CredentialPolicy.validateLoginId(loginId))
                .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ab",                  // 너무 짧음 (2자)
            "abcd",                // 너무 짧음 (4자)
            "abcdefghij1234567890_", // 21자 초과
            "user@name",           // 허용되지 않는 특수문자
            "user name",           // 공백 포함
            "한글닉네임"            // 비ASCII
        })
        @DisplayName("형식이 어긋나면 INVALID_LOGIN_ID_FORMAT 예외를 던진다")
        void 잘못된_형식이면_예외(String loginId) {
            assertThatThrownBy(() -> CredentialPolicy.validateLoginId(loginId))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_ID_FORMAT);
        }

        @Test
        @DisplayName("null 로그인 ID 는 예외를 던진다")
        void null이면_예외() {
            assertThatThrownBy(() -> CredentialPolicy.validateLoginId(null))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_ID_FORMAT);
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
