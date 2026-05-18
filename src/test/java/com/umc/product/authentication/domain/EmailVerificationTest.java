package com.umc.product.authentication.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * EmailVerification 도메인 단위 테스트.
 * <p>
 * verifyCode 의 brute-force 방어(시도 횟수 / 임계치 초과 시 즉시 만료), regenerate 의 초기화,
 * purpose 영속을 검증한다.
 */
class EmailVerificationTest {

    private static final String EMAIL = "alice@example.com";
    private static final String CODE = "123456";
    private static final String TOKEN = "00000000-0000-0000-0000-000000000000";

    private EmailVerification newSession(EmailVerificationPurpose purpose) {
        return EmailVerification.builder()
            .email(EMAIL)
            .code(CODE)
            .token(TOKEN)
            .purpose(purpose)
            .build();
    }

    @Nested
    @DisplayName("새 세션 생성")
    class Create {

        @Test
        @DisplayName("attemptCount 는 0 으로, isVerified 는 false 로 시작한다")
        void 초기_상태_검증() {
            // given / when
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);

            // then
            assertThat(session.getAttemptCount()).isZero();
            assertThat(session.isVerified()).isFalse();
            assertThat(session.getPurpose()).isEqualTo(EmailVerificationPurpose.REGISTER);
            assertThat(session.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("purpose 는 빌더에 전달된 값으로 고정된다")
        void purpose_고정() {
            // given / when
            EmailVerification session = newSession(EmailVerificationPurpose.PASSWORD_RESET);

            // then
            assertThat(session.getPurpose()).isEqualTo(EmailVerificationPurpose.PASSWORD_RESET);
        }
    }

    @Nested
    @DisplayName("verifyCode")
    class VerifyCode {

        @Test
        @DisplayName("올바른 코드와 미만료 상태이면 검증 성공한다")
        void 정상_검증_성공() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);

            // when
            session.verifyCode(CODE);

            // then
            assertThat(session.isVerified()).isTrue();
            assertThat(session.getVerifiedAt()).isNotNull();
            assertThat(session.getVerifiedBy()).isEqualTo("CODE");
            assertThat(session.getAttemptCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("틀린 코드면 attemptCount 가 1 증가하고 INVALID_EMAIL_VERIFICATION 예외를 던진다")
        void 틀린_코드_시도_횟수_증가() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);

            // when / then
            assertThatThrownBy(() -> session.verifyCode("000000"))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
            assertThat(session.getAttemptCount()).isEqualTo(1);
            assertThat(session.isVerified()).isFalse();
        }

        @Test
        @DisplayName("MAX_ATTEMPT_COUNT 회 실패 시 세션이 즉시 만료된다")
        void 임계치_도달_시_세션_무효화() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);

            // when: 5회 연속 실패
            for (int i = 0; i < EmailVerification.MAX_ATTEMPT_COUNT; i++) {
                assertThatThrownBy(() -> session.verifyCode("000000"))
                    .isInstanceOf(AuthenticationDomainException.class);
            }

            // then
            assertThat(session.getAttemptCount()).isEqualTo(EmailVerification.MAX_ATTEMPT_COUNT);
            assertThat(session.isExpired()).isTrue();
        }

        @Test
        @DisplayName("임계치 도달 후에는 정답을 입력해도 검증되지 않으며 attemptCount 가 더 이상 증가하지 않는다")
        void 임계치_도달_후_정답도_거부() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);
            for (int i = 0; i < EmailVerification.MAX_ATTEMPT_COUNT; i++) {
                assertThatThrownBy(() -> session.verifyCode("000000"))
                    .isInstanceOf(AuthenticationDomainException.class);
            }
            int countBefore = session.getAttemptCount();

            // when / then
            assertThatThrownBy(() -> session.verifyCode(CODE))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
            assertThat(session.getAttemptCount()).isEqualTo(countBefore);
            assertThat(session.isVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("regenerate")
    class Regenerate {

        @Test
        @DisplayName("코드 재발급 시 attemptCount 가 0 으로 초기화된다")
        void 재발급_시_attemptCount_초기화() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);
            assertThatThrownBy(() -> session.verifyCode("000000"))
                .isInstanceOf(AuthenticationDomainException.class);
            assertThat(session.getAttemptCount()).isEqualTo(1);

            // when
            session.regenerate("999999", "new-token");

            // then
            assertThat(session.getAttemptCount()).isZero();
            assertThat(session.getCode()).isEqualTo("999999");
            assertThat(session.getToken()).isEqualTo("new-token");
        }

        @Test
        @DisplayName("이미 검증된 세션은 ALREADY_VERIFIED_EMAIL 예외로 재발급 불가")
        void 검증_완료_세션_재발급_금지() {
            // given
            EmailVerification session = newSession(EmailVerificationPurpose.REGISTER);
            session.verifyCode(CODE);

            // when / then
            assertThatThrownBy(() -> session.regenerate("999999", "new-token"))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.ALREADY_VERIFIED_EMAIL);
        }
    }
}
