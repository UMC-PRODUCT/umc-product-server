package com.umc.product.common.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("ChallengerPart")
class ChallengerPartTest {

    @Test
    @DisplayName("선택 가능한 파트는 신규 발급 파트(PLAN/DESIGN/WEB_PE/MOBILE_PE)만 sortOrder 순으로 반환한다")
    void selectableValues_신규_파트만_정렬() {
        List<ChallengerPart> result = ChallengerPart.selectableValues();

        assertThat(result).containsExactly(
            ChallengerPart.PLAN,
            ChallengerPart.DESIGN,
            ChallengerPart.WEB_PRODUCT_ENGINEER,
            ChallengerPart.MOBILE_PRODUCT_ENGINEER
        );
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerPart.class,
        names = {"PLAN", "DESIGN", "WEB_PRODUCT_ENGINEER", "MOBILE_PRODUCT_ENGINEER"})
    @DisplayName("신규 파트는 isSelectable=true")
    void isSelectable_신규_파트_true(ChallengerPart part) {
        assertThat(part.isSelectable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerPart.class,
        names = {"WEB", "ANDROID", "IOS", "NODEJS", "SPRINGBOOT", "ADMIN"})
    @DisplayName("레거시 파트는 isSelectable=false")
    void isSelectable_레거시_파트_false(ChallengerPart part) {
        assertThat(part.isSelectable()).isFalse();
    }
}
