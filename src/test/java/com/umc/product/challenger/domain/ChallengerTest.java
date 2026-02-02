package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChallengerTest {
    Challenger challenger;

    @BeforeEach
    void setUp() {
        challenger = Challenger.builder()
                .memberId(1L)
                .part(ChallengerPart.SPRINGBOOT)
                .gisuId(9L)
                .build();
    }

    @Test
    void 챌린저_생성_시_기본적으로_활성화_상태이다() {
        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
    }

    @Test
    void 활성_상태가_아닌_챌린저는_상태를_변경할_수_없다() {
        // given

        // when

        // then

    }

    @DisplayName("챌린저 상태를 변경할 때는 도메인 메서드를 이용해야 한다")
    @Test
    void shouldUseDomainMethodWhenChangingStatus() {
        // given

        // when

        // then

    }
}
