package com.umc.product.challenger.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;

@DisplayName("Challenger 파트 DTO")
class ChallengerPartDtoTest {

    @Test
    @DisplayName("조회 Info는 단일 파트와 인프라 여부를 노출한다")
    void 조회_Info는_단일_파트와_인프라_여부를_노출한다() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.MOBILE_PRODUCT_ENGINEER)
            .infra(true)
            .gisuId(9L)
            .build();

        ChallengerInfo info = ChallengerInfo.from(challenger, List.of());
        ChallengerBasicInfo basicInfo = ChallengerBasicInfo.from(challenger);

        assertThat(info.part()).isEqualTo(ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        assertThat(info.infra()).isTrue();
        assertThat(basicInfo.part()).isEqualTo(info.part());
        assertThat(basicInfo.infra()).isEqualTo(info.infra());
    }

    @Test
    @DisplayName("레거시 파트도 변환 없이 그대로 노출한다")
    void 레거시_파트도_변환_없이_그대로_노출한다() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(9L)
            .build();

        ChallengerInfo info = ChallengerInfo.from(challenger, List.of());

        assertThat(info.part()).isEqualTo(ChallengerPart.SPRINGBOOT);
        assertThat(info.infra()).isFalse();
    }
}
