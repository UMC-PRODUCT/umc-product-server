package com.umc.product.authentication.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CredentialAvailabilityQueryServiceTest {

    @Mock
    GetMemberCredentialUseCase getMemberCredentialUseCase;

    @InjectMocks
    CredentialAvailabilityQueryService service;

    @Test
    @DisplayName("정상 형식의 미사용 ID 는 사용 가능으로 판정한다")
    void 미사용_ID_는_사용가능() {
        // given
        given(getMemberCredentialUseCase.existsByLoginId("alice01")).willReturn(false);

        // when
        boolean available = service.isLoginIdAvailable("alice01");

        // then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("정상 형식이지만 이미 사용 중인 ID 는 사용 불가로 판정한다")
    void 이미_사용중인_ID_는_사용불가() {
        // given
        given(getMemberCredentialUseCase.existsByLoginId("alice01")).willReturn(true);

        // when
        boolean available = service.isLoginIdAvailable("alice01");

        // then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("형식이 잘못된 ID 는 사용 가능 여부를 묻지 않고 INVALID_LOGIN_ID_FORMAT 예외를 던진다")
    void 잘못된_형식이면_예외() {
        // when & then
        assertThatThrownBy(() -> service.isLoginIdAvailable("ab"))
            .isInstanceOf(AuthenticationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(AuthenticationErrorCode.INVALID_LOGIN_ID_FORMAT);

        then(getMemberCredentialUseCase).should(never()).existsByLoginId(ArgumentMatchers.anyString());
    }
}
