package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.organization.application.port.in.query.CheckSchoolExistenceUseCase;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("학교별 닉네임 사용 가능 여부 판정")
class NicknameAvailabilityQueryServiceTest {

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    CheckSchoolExistenceUseCase checkSchoolExistenceUseCase;

    @InjectMocks
    NicknameAvailabilityQueryService service;

    @Test
    @DisplayName("선택한 학교에서 미사용인 닉네임은 사용 가능하다")
    void 미사용_닉네임은_사용_가능하다() {
        // given
        given(checkSchoolExistenceUseCase.existsById(7L)).willReturn(true);
        given(loadMemberPort.existsNicknameInSchool(7L, "토리")).willReturn(false);

        // when
        var result = service.check(7L, "토리");

        // then
        assertThat(result.available()).isTrue();
    }

    @Test
    @DisplayName("선택한 학교에서 사용 중인 닉네임은 사용 불가하다")
    void 사용_중인_닉네임은_사용_불가하다() {
        // given
        given(checkSchoolExistenceUseCase.existsById(7L)).willReturn(true);
        given(loadMemberPort.existsNicknameInSchool(7L, "토리")).willReturn(true);

        // when
        var result = service.check(7L, "토리");

        // then
        assertThat(result.available()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 학교는 사용 가능 여부를 판정하지 않는다")
    void 없는_학교는_조회하지_않는다() {
        // given
        given(checkSchoolExistenceUseCase.existsById(7L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> service.check(7L, "토리"))
            .isInstanceOf(OrganizationDomainException.class)
            .extracting("baseCode").isEqualTo(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        then(loadMemberPort).shouldHaveNoInteractions();
    }
}
