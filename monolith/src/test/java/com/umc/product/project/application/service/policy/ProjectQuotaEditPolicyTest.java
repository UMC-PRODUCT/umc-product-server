package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

class ProjectQuotaEditPolicyTest {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    private final ProjectQuotaEditPolicy sut = new ProjectQuotaEditPolicy();

    @ParameterizedTest
    @CsvSource({"-101,true", "-100,false", "-50,false", "0,false", "100,false", "101,true"})
    void 첫_시작과_3차_판정마감을_포함하고_차수_사이에도_잠근다(long offset, boolean allowed) {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60),
            round(MatchingType.PLAN_DESIGN, MatchingPhase.THIRD, 50, 80, 100));

        // when
        var result = sut.evaluate(ChallengerPart.DESIGN, rounds, NOW.plusSeconds(offset));

        // then
        assertThat(result.capability().allowed()).isEqualTo(allowed);
        if (!allowed) {
            assertThat(result.editableAfter()).isEqualTo(NOW.plusSeconds(100));
        }
    }

    @Test
    void 디자인과_개발의_일정은_각각_판정하고_PM은_진행중인_유형이_있으면_잠근다() {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60),
            round(MatchingType.PLAN_DESIGN, MatchingPhase.THIRD, -50, -40, -30),
            round(MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -20, 10, 20));

        // when & then
        assertThat(sut.evaluate(ChallengerPart.DESIGN, rounds, NOW).capability().allowed()).isTrue();
        assertThat(sut.evaluate(ChallengerPart.WEB_PRODUCT_ENGINEER, rounds, NOW).capability().allowed()).isFalse();
        assertThat(sut.evaluate(ChallengerPart.PLAN, rounds, NOW).capability().allowed()).isFalse();
    }

    @Test
    void 시작후_3차_일정이_없으면_임의로_잠금을_풀지_않는다() {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60));

        // when
        var result = sut.evaluate(ChallengerPart.DESIGN, rounds, NOW);

        // then
        assertThat(result.capability().reasonCode())
            .isEqualTo(ProjectPermissionReason.PART_QUOTA_MATCHING_SCHEDULE_INCOMPLETE.name());
        assertThat(result.editableAfter()).isNull();
    }

    @Test
    void 일정이_없거나_랜덤매칭만_있으면_수정할_수_있다() {
        // given
        var random = ProjectMatchingRound.create("랜덤", null, MatchingType.PLAN_DESIGN, MatchingPhase.RANDOM_MATCHING,
            1L, NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(600));

        // when & then
        assertThat(sut.evaluate(ChallengerPart.DESIGN, List.of(), NOW).capability().allowed()).isTrue();
        assertThat(sut.evaluate(ChallengerPart.DESIGN, List.of(random), NOW).capability().allowed()).isTrue();
    }

    private ProjectMatchingRound round(MatchingType type, MatchingPhase phase, long starts, long ends, long deadline) {
        return ProjectMatchingRound.create("차수", null, type, phase, 1L, NOW.plusSeconds(starts), NOW.plusSeconds(ends),
            NOW.plusSeconds(deadline), NOW.plusSeconds(deadline + 3600));
    }
}
