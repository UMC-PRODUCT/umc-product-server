package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectMatchingAdjustmentPolicyTest {

    private static final Instant START = Instant.parse("2026-11-19T00:00:00+09:00");
    private final ProjectMatchingAdjustmentPolicy policy = new ProjectMatchingAdjustmentPolicy();

    @Test
    void 일반_차수의_결정_마감_전에는_즉시_반영하도록_빈_값을_반환한다() {
        // given
        ProjectMatchingRound round = regularRound();

        // when & then
        assertThat(policy.validateChange(List.of(round), MatchingType.PLAN_DESIGN,
            round.getDecisionDeadline().minusNanos(1))).isEmpty();
    }

    @Test
    void 일반_차수의_결정_마감부터_공개_완료_전까지는_변경을_차단한다() {
        // given
        ProjectMatchingRound round = regularRound();
        List<ProjectMatchingRound> rounds = List.of(round);

        // when & then
        assertBlocked(rounds, round.getDecisionDeadline());
        assertBlocked(rounds, round.getResultPublishAt());
    }

    @Test
    void 일반_차수의_공개_완료_후에는_즉시_반영하도록_빈_값을_반환한다() {
        // given
        ProjectMatchingRound round = regularRound();
        round.executeAutoDecision(null);
        round.publishResult(round.getResultPublishAt());

        // when & then
        assertThat(policy.validateChange(List.of(round), MatchingType.PLAN_DESIGN, round.getResultPublishAt())).isEmpty();
    }

    @Test
    void 조정_시작_시각이_되어도_랜덤_매칭이_완료되지_않으면_변경을_차단한다() {
        // given
        ProjectMatchingRound round = randomRound();

        // when & then
        assertBlocked(List.of(publishedThirdRound(), round), round.getStartsAt());
    }

    @Test
    void 랜덤_매칭이_완료되어도_조정_시작_전에는_변경을_차단한다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);

        // when & then
        assertBlocked(List.of(publishedThirdRound(), round), round.getStartsAt().minusNanos(1));
    }

    @Test
    void 랜덤_매칭이_완료된_조정_기간에는_일괄_공개할_랜덤_차수를_반환한다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), round);

        // when & then
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, START)).contains(round);
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, round.getEndsAt().minusNanos(1))).contains(round);
    }

    @Test
    void 조정_종료부터_최종_공개_완료_전까지는_변경을_차단한다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), round);

        // when & then
        assertBlocked(rounds, round.getEndsAt());
        assertBlocked(rounds, round.getResultPublishAt());
    }

    @Test
    void 최종_공개_완료_후에는_즉시_반영하도록_빈_값을_반환한다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);
        round.publishResult(round.getResultPublishAt());

        // when & then
        assertThat(policy.validateChange(List.of(publishedThirdRound(), round), MatchingType.PLAN_DESIGN,
            round.getResultPublishAt())).isEmpty();
    }

    @Test
    void 같은_유형의_3차_공개_후에는_랜덤_매칭_실행_전에도_변경을_차단한다() {
        // given
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), randomRound());

        // when & then
        assertBlocked(rounds, START.minusSeconds(30));
    }

    @Test
    void 디자인_최종_공개_후에는_개발자_조정_기간에도_디자인_변경을_즉시_반영한다() {
        // given
        ProjectMatchingRound designRandom = randomRound();
        designRandom.executeAutoDecision(null);
        designRandom.publishResult(designRandom.getResultPublishAt());
        Instant developerStart = START.plusSeconds(30L * 24 * 3600);
        ProjectMatchingRound developerThird = ProjectMatchingRound.create("개발자 3차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.THIRD, 1L, developerStart.minusSeconds(10800), developerStart.minusSeconds(7200),
            developerStart.minusSeconds(3600), developerStart.minusSeconds(60));
        developerThird.executeAutoDecision(null);
        developerThird.publishResult(developerThird.getResultPublishAt());
        ProjectMatchingRound developerRandom = ProjectMatchingRound.create("개발자 랜덤", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.RANDOM_MATCHING, 1L, developerStart, developerStart.plusSeconds(7200),
            developerStart.plusSeconds(7200), developerStart.plusSeconds(10800));
        developerRandom.executeAutoDecision(null);
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), designRandom, developerThird, developerRandom);

        // when & then
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, developerStart)).isEmpty();
    }

    @Test
    void 조정_종료_후에도_최종_공개_전에는_조정_명단을_조회할_수_있다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);

        // when & then
        assertThat(policy.findViewableAdjustmentRounds(List.of(publishedThirdRound(), round), round.getEndsAt()))
            .containsExactly(round);
    }

    @Test
    void 최종_공개_완료_후에는_조정_명단을_조회하지_않는다() {
        // given
        ProjectMatchingRound round = randomRound();
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), round);
        round.executeAutoDecision(null);
        round.publishResult(round.getResultPublishAt());

        // when & then
        assertThat(policy.findViewableAdjustmentRounds(rounds, round.getResultPublishAt())).isEmpty();
    }

    private void assertBlocked(List<ProjectMatchingRound> rounds, Instant now) {
        assertThatThrownBy(() -> policy.validateChange(rounds, MatchingType.PLAN_DESIGN, now))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
    }

    private ProjectMatchingRound regularRound() {
        return ProjectMatchingRound.create("1차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.FIRST, 1L, START, START.plusSeconds(3600), START.plusSeconds(7200), START.plusSeconds(10800));
    }

    private ProjectMatchingRound publishedThirdRound() {
        ProjectMatchingRound third = ProjectMatchingRound.create("3차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.THIRD, 1L, START.minusSeconds(10800), START.minusSeconds(7200),
            START.minusSeconds(3600), START.minusSeconds(60));
        third.executeAutoDecision(null);
        third.publishResult(START.minusSeconds(60));
        return third;
    }

    private ProjectMatchingRound randomRound() {
        return ProjectMatchingRound.create("랜덤", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.RANDOM_MATCHING, 1L, START, START.plusSeconds(7200), START.plusSeconds(7200),
            START.plusSeconds(10800));
    }
}
