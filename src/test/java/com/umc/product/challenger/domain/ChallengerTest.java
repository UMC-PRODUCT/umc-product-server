package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

@DisplayName("Challenger 도메인")
class ChallengerTest {

    @Test
    @DisplayName("챌린저는 단일 파트와 infra 비활성 상태로 생성된다")
    void 챌린저는_단일_파트와_infra_비활성_상태로_생성된다() {
        Challenger challenger = challenger(ChallengerPart.SPRINGBOOT, false);

        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        assertThat(challenger.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT);
        assertThat(challenger.isInfra()).isFalse();
    }

    @Test
    @DisplayName("웹과 모바일 프로덕트 엔지니어는 infra를 함께 수강할 수 있다")
    void 개발_파트는_infra를_함께_수강할_수_있다() {
        assertThat(challenger(ChallengerPart.WEB_PRODUCT_ENGINEER, true).isInfra()).isTrue();
        assertThat(challenger(ChallengerPart.MOBILE_PRODUCT_ENGINEER, true).isInfra()).isTrue();
    }

    @Test
    @DisplayName("개발 파트가 아니면 infra를 함께 수강할 수 없다")
    void 개발_파트가_아니면_infra를_함께_수강할_수_없다() {
        assertThatThrownBy(() -> challenger(ChallengerPart.DESIGN, true))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
    }

    @Test
    @DisplayName("INFRA는 챌린저의 단독 파트가 될 수 없다")
    void INFRA는_챌린저의_단독_파트가_될_수_없다() {
        assertThatThrownBy(() -> challenger(ChallengerPart.INFRA, false))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
    }

    @Test
    @DisplayName("수강 없이 기수에 소속된 챌린저에게 파트와 infra를 추가한다")
    void 수강_없는_소속에_파트와_infra를_추가한다() {
        Challenger challenger = Challenger.createWithoutEnrollment(1L, 9L);

        boolean changed = challenger.applyLearning(ChallengerPart.MOBILE_PRODUCT_ENGINEER, true);

        assertThat(changed).isTrue();
        assertThat(challenger.getPart()).isEqualTo(ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        assertThat(challenger.isInfra()).isTrue();
    }

    @Test
    @DisplayName("기존 파트와 다른 파트를 추가할 수 없다")
    void 기존_파트와_다른_파트를_추가할_수_없다() {
        Challenger challenger = challenger(ChallengerPart.WEB_PRODUCT_ENGINEER, false);

        assertThatThrownBy(() -> challenger.applyLearning(ChallengerPart.DESIGN, false))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
    }

    @Test
    @DisplayName("infra를 지원하지 않는 파트로 변경하면 infra가 해제된다")
    void 파트_변경_시_infra_정합성을_유지한다() {
        Challenger challenger = challenger(ChallengerPart.WEB_PRODUCT_ENGINEER, true);

        challenger.changePart(ChallengerPart.DESIGN);

        assertThat(challenger.getPart()).isEqualTo(ChallengerPart.DESIGN);
        assertThat(challenger.isInfra()).isFalse();
    }

    @Test
    @DisplayName("비활성 챌린저는 파트를 변경할 수 없다")
    void 비활성_챌린저는_파트를_변경할_수_없다() {
        Challenger challenger = challenger(ChallengerPart.SPRINGBOOT, false);
        challenger.changeStatus(ChallengerStatus.WITHDRAWN, 99L, "탈부");

        assertThatThrownBy(() -> challenger.changePart(ChallengerPart.WEB))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);
    }

    private Challenger challenger(ChallengerPart part, boolean infra) {
        return Challenger.builder().memberId(1L).part(part).infra(infra).gisuId(9L).build();
    }
}
