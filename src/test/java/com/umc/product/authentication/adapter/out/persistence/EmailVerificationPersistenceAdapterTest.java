package com.umc.product.authentication.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.authentication.domain.EmailVerification;
import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EmailVerificationPersistenceAdapter 단위 테스트.
 * <p>
 * CLAUDE.md 의 get[By] 시멘틱(없으면 예외) 을 충족하는지, QueryRepository 의 Optional 빈 결과를
 * 도메인 예외로 변환하는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationPersistenceAdapterTest {

    private static final Long SESSION_ID = 100L;
    private static final String TOKEN = "11111111-1111-1111-1111-111111111111";

    @Mock
    EmailVerificationJpaRepository jpaRepository;

    @Mock
    EmailVerificationQueryRepository queryRepository;

    @InjectMocks
    EmailVerificationPersistenceAdapter adapter;

    private EmailVerification newSession() {
        return EmailVerification.builder()
            .email("alice@example.com")
            .code("123456")
            .token(TOKEN)
            .purpose(EmailVerificationPurpose.REGISTER)
            .build();
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("존재하면 엔티티를 반환한다")
        void 존재_시_엔티티_반환() {
            // given
            EmailVerification session = newSession();
            given(queryRepository.findById(SESSION_ID)).willReturn(Optional.of(session));

            // when
            EmailVerification result = adapter.getById(SESSION_ID);

            // then
            assertThat(result).isSameAs(session);
        }

        @Test
        @DisplayName("미존재 시 INVALID_EMAIL_VERIFICATION 예외를 던진다")
        void 미존재_시_예외() {
            // given
            given(queryRepository.findById(SESSION_ID)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> adapter.getById(SESSION_ID))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
        }
    }

    @Nested
    @DisplayName("getByToken")
    class GetByToken {

        @Test
        @DisplayName("존재하면 엔티티를 반환한다")
        void 존재_시_엔티티_반환() {
            // given
            EmailVerification session = newSession();
            given(queryRepository.findByToken(TOKEN)).willReturn(Optional.of(session));

            // when
            EmailVerification result = adapter.getByToken(TOKEN);

            // then
            assertThat(result).isSameAs(session);
        }

        @Test
        @DisplayName("미존재 시 INVALID_EMAIL_VERIFICATION 예외를 던진다")
        void 미존재_시_예외() {
            // given
            given(queryRepository.findByToken(TOKEN)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> adapter.getByToken(TOKEN))
                .isInstanceOf(AuthenticationDomainException.class)
                .extracting("baseCode")
                .isEqualTo(AuthenticationErrorCode.INVALID_EMAIL_VERIFICATION);
        }
    }
}
