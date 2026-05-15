package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class PartAssignmentPolicyTest {

    private static final Set<ChallengerPart> FRONTEND = Set.of(
        ChallengerPart.WEB, ChallengerPart.ANDROID, ChallengerPart.IOS
    );
    private static final Set<ChallengerPart> BACKEND = Set.of(
        ChallengerPart.NODEJS, ChallengerPart.SPRINGBOOT
    );

    PartAssignmentPolicy sut = new PartAssignmentPolicy();

    @Test
    @DisplayName("첫 슬롯은 항상 PLAN")
    void 첫_슬롯_PLAN() {
        for (int i = 0; i < 50; i++) {
            assertThat(sut.nextProjectSlots().get(0)).isEqualTo(ChallengerPart.PLAN);
        }
    }

    @RepeatedTest(20)
    @DisplayName("슬롯 총원은 11~13 명, PLAN 1 + FE 5~6 + BE 5~6")
    void 슬롯_분포_유효성() {
        // When
        List<ChallengerPart> slots = sut.nextProjectSlots();

        // Then
        assertThat(slots.size()).isBetween(11, 13);
        long plan = slots.stream().filter(p -> p == ChallengerPart.PLAN).count();
        long fe = slots.stream().filter(FRONTEND::contains).count();
        long be = slots.stream().filter(BACKEND::contains).count();
        assertThat(plan).isEqualTo(1);
        assertThat(fe).isBetween(5L, 6L);
        assertThat(be).isBetween(5L, 6L);
    }

    @Test
    @DisplayName("프론트엔드/백엔드 파트 외의 값은 PLAN 외에 등장하지 않는다")
    void 허용된_파트만_등장() {
        for (int i = 0; i < 50; i++) {
            List<ChallengerPart> slots = sut.nextProjectSlots();
            for (int j = 1; j < slots.size(); j++) {
                ChallengerPart part = slots.get(j);
                assertThat(FRONTEND.contains(part) || BACKEND.contains(part))
                    .as("slot %d part %s must be FE or BE", j, part)
                    .isTrue();
            }
        }
    }
}
