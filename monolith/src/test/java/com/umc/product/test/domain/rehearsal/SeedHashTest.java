package com.umc.product.test.domain.rehearsal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.test.domain.rehearsal.SeedHash.Purpose;

@DisplayName("결정 난수")
class SeedHashTest {

    @Test
    @DisplayName("같은 seed·목적·키면 같은 값이고, 값은 [0, 1)에 고르게 퍼진다")
    void sameInputSameValueAndUniform() {
        double mean = IntStream.range(0, 10_000)
            .mapToDouble(index -> SeedHash.unit(7L, Purpose.APPLY_SELECT, "member" + index))
            .peek(value -> assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(1))
            .average().orElseThrow();

        assertThat(SeedHash.unit(7L, Purpose.APPLY_SELECT, "a@qa.test"))
            .isEqualTo(SeedHash.unit(7L, Purpose.APPLY_SELECT, "a@qa.test"));
        assertThat(SeedHash.unit(7L, Purpose.APPLY_SELECT, "a@qa.test"))
            .isNotEqualTo(SeedHash.unit(7L, Purpose.PROJECT_PICK, "a@qa.test"));
        assertThat(mean).isCloseTo(0.5, within(0.02));
    }

    @Test
    @DisplayName("기본 seed는 지부·유형·차수 번호로만 정해진다")
    void defaultSeedDependsOnChapterTypeAndPhase() {
        long first = SeedHash.defaultSeed("쿼카", MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST);

        assertThat(SeedHash.defaultSeed("쿼카", MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST)).isEqualTo(first);
        assertThat(SeedHash.defaultSeed("쿼카", MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND)).isNotEqualTo(first);
        assertThat(SeedHash.defaultSeed("수달", MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST)).isNotEqualTo(first);
    }
}
