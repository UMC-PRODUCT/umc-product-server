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
    @DisplayName("풀이 충분히 클 때 첫 슬롯은 항상 PLAN")
    void 첫_슬롯_PLAN() {
        for (int i = 0; i < 50; i++) {
            assertThat(sut.nextProjectSlots(20).get(0)).isEqualTo(ChallengerPart.PLAN);
        }
    }

    @RepeatedTest(20)
    @DisplayName("풀이 13 이상이면 슬롯 총원 11~13, PLAN 1 + FE 5~6 + BE 5~6")
    void 풀_충분_시_슬롯_분포() {
        // When
        List<ChallengerPart> slots = sut.nextProjectSlots(20);

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
    @DisplayName("풀이 MIN_TOTAL(11) 미만이면 빈 슬롯 리스트를 반환한다")
    void 풀_부족_시_빈_리스트() {
        for (int pool = 0; pool < PartAssignmentPolicy.MIN_TOTAL; pool++) {
            assertThat(sut.nextProjectSlots(pool))
                .as("pool=%d", pool)
                .isEmpty();
        }
    }

    @RepeatedTest(20)
    @DisplayName("풀이 정확히 11 이면 (PLAN, FE 5, BE 5) 고정 분포")
    void 풀_11_시_최소_분포() {
        // When
        List<ChallengerPart> slots = sut.nextProjectSlots(11);

        // Then
        assertThat(slots).hasSize(11);
        long fe = slots.stream().filter(FRONTEND::contains).count();
        long be = slots.stream().filter(BACKEND::contains).count();
        assertThat(fe).isEqualTo(5L);
        assertThat(be).isEqualTo(5L);
    }

    @RepeatedTest(20)
    @DisplayName("풀이 12 이면 슬롯 총원이 12 를 넘지 않는다")
    void 풀_12_시_총원_보존() {
        // When
        List<ChallengerPart> slots = sut.nextProjectSlots(12);

        // Then
        assertThat(slots).hasSizeBetween(11, 12);
    }

    @Test
    @DisplayName("프론트엔드/백엔드 파트 외의 값은 PLAN 외에 등장하지 않는다")
    void 허용된_파트만_등장() {
        for (int i = 0; i < 50; i++) {
            List<ChallengerPart> slots = sut.nextProjectSlots(20);
            for (int j = 1; j < slots.size(); j++) {
                ChallengerPart part = slots.get(j);
                assertThat(FRONTEND.contains(part) || BACKEND.contains(part))
                    .as("slot %d part %s must be FE or BE", j, part)
                    .isTrue();
            }
        }
    }
}
