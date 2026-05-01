package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Member 도메인의 ID/PW 자격증명 관련 메서드 단위 테스트.
 */
class MemberCredentialTest {

    private static final String LOGIN_ID = "alice01";
    private static final String ENCODED_PASSWORD = "{argon2}$argon2id$v=19$m=16384,t=2,p=1$abc$def";

    Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
            .name("앨리스")
            .nickname("alice")
            .email("alice@test.com")
            .build();
    }

    @Nested
    @DisplayName("회원 자격 증명 등록 (ID/PW)")
    class RegisterCredential {

        @Test
        @DisplayName("자격증명이 없는 회원은 최초 등록에 성공한다")
        void 자격증명_최초_등록_성공() {
            // when
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);

            // then
            assertThat(member.getLoginId()).isEqualTo(LOGIN_ID);
            assertThat(member.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
            assertThat(member.hasCredential()).isTrue();
        }

        @Test
        @DisplayName("이미 자격증명이 등록된 회원은 재등록을 거부한다")
        void 이미_등록된_경우_예외() {
            // given
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);

            // when & then
            assertThatThrownBy(() -> member.registerCredential("bob02", ENCODED_PASSWORD))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.CREDENTIAL_ALREADY_REGISTERED);
        }

        @Test
        @DisplayName("비활성화 회원은 자격증명 등록을 거부한다")
        void 비활성_회원이면_예외() {
            // given
            ReflectionTestUtils.setField(member, "status", MemberStatus.INACTIVE);

            // when & then
            assertThatThrownBy(() -> member.registerCredential(LOGIN_ID, ENCODED_PASSWORD))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_ACTIVE);
        }

        @Test
        @DisplayName("loginId 가 null 이나 blank 이면 등록을 거부한다")
        void loginId_유효성_검증() {
            assertThatThrownBy(() -> member.registerCredential(null, ENCODED_PASSWORD))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_LOGIN_ID);

            assertThatThrownBy(() -> member.registerCredential(" ", ENCODED_PASSWORD))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_LOGIN_ID);
        }

        @Test
        @DisplayName("encodedPassword 가 null 이나 blank 이면 등록을 거부한다")
        void password_유효성_검증() {
            assertThatThrownBy(() -> member.registerCredential(LOGIN_ID, null))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD);

            assertThatThrownBy(() -> member.registerCredential(LOGIN_ID, ""))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("자격증명이 등록된 회원은 비밀번호를 변경할 수 있다")
        void 비밀번호_변경_성공() {
            // given
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);
            String newEncoded = "{argon2}$argon2id$v=19$m=16384,t=2,p=1$xxx$yyy";

            // when
            member.changePassword(newEncoded);

            // then
            assertThat(member.getPasswordHash()).isEqualTo(newEncoded);
            // loginId 는 유지된다
            assertThat(member.getLoginId()).isEqualTo(LOGIN_ID);
        }

        @Test
        @DisplayName("자격증명이 등록되지 않은 회원은 비밀번호 변경을 거부한다")
        void 자격증명_미등록이면_예외() {
            // when & then
            assertThatThrownBy(() -> member.changePassword(ENCODED_PASSWORD))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.CREDENTIAL_NOT_REGISTERED);
        }

        @Test
        @DisplayName("비활성화 회원은 비밀번호 변경을 거부한다")
        void 비활성_회원이면_예외() {
            // given
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);
            ReflectionTestUtils.setField(member, "status", MemberStatus.INACTIVE);

            // when & then
            assertThatThrownBy(() -> member.changePassword("{argon2}new"))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_ACTIVE);
        }

        @Test
        @DisplayName("변경할 비밀번호가 null 이나 blank 이면 거부한다")
        void password_유효성_검증() {
            // given
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);

            // when & then
            assertThatThrownBy(() -> member.changePassword(null))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD);

            assertThatThrownBy(() -> member.changePassword("  "))
                .isInstanceOf(MemberDomainException.class)
                .extracting("baseCode")
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD);
        }
    }

    @Nested
    @DisplayName("회원 자격 증명 보유 관련 검증")
    class HasCredential {

        @Test
        @DisplayName("loginId 와 passwordHash 가 모두 있으면 true 를 반환한다")
        void 둘_다_있으면_true() {
            member.registerCredential(LOGIN_ID, ENCODED_PASSWORD);

            assertThat(member.hasCredential()).isTrue();
        }

        @Test
        @DisplayName("OAuth 전용 회원처럼 자격증명이 없으면 false 를 반환한다")
        void 자격증명_미등록이면_false() {
            assertThat(member.hasCredential()).isFalse();
        }
    }
}
