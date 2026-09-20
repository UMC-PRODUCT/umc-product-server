package com.umc.product.common.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChallengerTrack")
class ChallengerTrackTest {

    @Test
    @DisplayName("모집 트랙은 기본 파트와 같은 이름을 유지한다")
    void 모집_트랙은_기본_파트와_같은_이름을_유지한다() {
        assertThat(ChallengerTrack.PLAN.name()).isEqualTo(ChallengerPart.PLAN.name());
        assertThat(ChallengerTrack.DESIGN.name()).isEqualTo(ChallengerPart.DESIGN.name());
        assertThat(ChallengerTrack.WEB_PRODUCT_ENGINEER.name())
            .isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER.name());
        assertThat(ChallengerTrack.MOBILE_PRODUCT_ENGINEER.name())
            .isEqualTo(ChallengerPart.MOBILE_PRODUCT_ENGINEER.name());
    }

    @Test
    @DisplayName("인프라 플러스는 모집 전용 트랙으로 유지한다")
    void 인프라_플러스는_모집_전용_트랙으로_유지한다() {
        assertThat(ChallengerTrack.values())
            .extracting(Enum::name)
            .contains("INFRA_PLUS")
            .doesNotContain("INFRA_CORE");
    }
}
