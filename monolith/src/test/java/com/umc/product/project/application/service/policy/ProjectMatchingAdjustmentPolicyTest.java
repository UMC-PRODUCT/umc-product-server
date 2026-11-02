package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectMatchingAdjustmentPolicyTest {

    private static final Instant START = Instant.parse("2026-11-19T00:00:00+09:00");
    private final ProjectMatchingAdjustmentPolicy policy = new ProjectMatchingAdjustmentPolicy();

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 다음_차수_시작_전까지는_직전_차수를_기록하고_시작_정각부터_새_차수를_기록한다(MatchingType type) {
        // given
        ProjectMatchingRound first = assignmentRound(type, MatchingPhase.FIRST, START);
        ProjectMatchingRound second = assignmentRound(type, MatchingPhase.SECOND, START.plusSeconds(14400));
        ProjectMatchingRound third = assignmentRound(type, MatchingPhase.THIRD, START.plusSeconds(28800));
        MatchingType otherType = type == MatchingType.PLAN_DESIGN ? MatchingType.PLAN_DEVELOPER : MatchingType.PLAN_DESIGN;
        ProjectMatchingRound other = assignmentRound(otherType, MatchingPhase.THIRD, START.plusSeconds(18000));
        List<ProjectMatchingRound> rounds = List.of(third, other, first, second);

        // when & then
        assertThat(policy.getManualAssignmentRound(rounds, type, first.getStartsAt())).isSameAs(first);
        assertThat(policy.getManualAssignmentRound(rounds, type, second.getStartsAt().minusNanos(1))).isSameAs(first);
        assertThat(policy.getManualAssignmentRound(rounds, type, second.getStartsAt())).isSameAs(second);
        assertThat(policy.getManualAssignmentRound(rounds, type, third.getStartsAt().minusNanos(1))).isSameAs(second);
        assertThat(policy.getManualAssignmentRound(rounds, type, third.getStartsAt())).isSameAs(third);
    }

    @Test
    void 시작된_일반_차수가_없으면_배정_차수를_임의로_기록하지_않는다() {
        // given
        List<ProjectMatchingRound> rounds = List.of(regularRound(), randomRound());

        // when & then
        assertThatThrownBy(() -> policy.getManualAssignmentRound(rounds, MatchingType.PLAN_DESIGN, START.minusNanos(1)))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
    }

    private ProjectMatchingRound assignmentRound(MatchingType type, MatchingPhase phase, Instant startsAt) {
        return ProjectMatchingRound.create(phase.name(), null, type, phase, 1L,
            startsAt, startsAt.plusSeconds(3600), startsAt.plusSeconds(7200), startsAt.plusSeconds(10800));
    }

    @Test
    void 첫_지원_시작부터_결정_마감_전까지_변경을_허용한다() {
        // given
        ProjectMatchingRound first = regularRound();
        List<ProjectMatchingRound> rounds = List.of(first);

        // when & then
        assertBlocked(rounds, first.getStartsAt().minusNanos(1));
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, first.getStartsAt())).isEmpty();
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, first.getDecisionDeadline().minusNanos(1)))
            .isEmpty();
        assertBlocked(rounds, first.getDecisionDeadline());
    }

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 일반_차수의_허용_기간에는_같은_유형만_변경할_수_있다(MatchingType type) {
        // given
        ProjectMatchingRound round = ProjectMatchingRound.create("1차", null, type,
            MatchingPhase.FIRST, 1L, START, START.plusSeconds(3600), START.plusSeconds(7200), START.plusSeconds(10800));
        MatchingType other = type == MatchingType.PLAN_DESIGN ? MatchingType.PLAN_DEVELOPER : MatchingType.PLAN_DESIGN;

        // when & then
        assertThat(policy.validateChange(List.of(round), type, START)).isEmpty();
        assertThatThrownBy(() -> policy.validateChange(List.of(round), other, START))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
    }

    @Test
    void 이전_결과_공개부터_다음_결정_마감_전까지_변경을_허용한다() {
        // given
        ProjectMatchingRound round = regularRound();
        round.executeAutoDecision(null);

        ProjectMatchingRound second = ProjectMatchingRound.create("2차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.SECOND, 1L, START.plusSeconds(14400), START.plusSeconds(18000),
            START.plusSeconds(21600), START.plusSeconds(25200));
        List<ProjectMatchingRound> rounds = List.of(round, second);

        // when & then
        assertBlocked(rounds, round.getResultPublishAt());
        round.publishResult(round.getResultPublishAt());
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, round.getResultPublishAt())).isEmpty();
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DESIGN, second.getDecisionDeadline().minusNanos(1)))
            .isEmpty();
        assertBlocked(rounds, second.getDecisionDeadline());
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

    @ParameterizedTest
    @EnumSource(MatchingType.class)
    void 랜덤_조정_기간에는_같은_유형만_변경할_수_있다(MatchingType type) {
        // given
        ProjectMatchingRound round = ProjectMatchingRound.create("랜덤", null, type,
            MatchingPhase.RANDOM_MATCHING, 1L, START, START.plusSeconds(7200), START.plusSeconds(7200),
            START.plusSeconds(10800));
        round.executeAutoDecision(null);
        MatchingType other = type == MatchingType.PLAN_DESIGN ? MatchingType.PLAN_DEVELOPER : MatchingType.PLAN_DESIGN;
        Instant previousStart = START.minusSeconds(30L * 24 * 3600);
        ProjectMatchingRound published = ProjectMatchingRound.create("이전 유형 랜덤", null, other,
            MatchingPhase.RANDOM_MATCHING, 1L, previousStart, previousStart.plusSeconds(7200),
            previousStart.plusSeconds(7200), previousStart.plusSeconds(10800));
        published.executeAutoDecision(null);
        published.publishResult(published.getResultPublishAt());
        List<ProjectMatchingRound> rounds = List.of(published, round);

        // when & then
        assertThat(policy.validateChange(rounds, type, START)).contains(round);
        assertThat(policy.validateChange(rounds, type, round.getEndsAt().minusNanos(1))).contains(round);
        assertThatThrownBy(() -> policy.validateChange(rounds, other, START))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
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
    void 디자인_최종_공개_완료_후에는_디자인_변경을_차단한다() {
        // given
        ProjectMatchingRound round = randomRound();
        round.executeAutoDecision(null);
        round.publishResult(round.getResultPublishAt());

        // when & then
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), round);
        assertThat(policy.isChangeAllowedInPeriod(rounds, round.getResultPublishAt())).isFalse();
        assertBlocked(rounds, round.getResultPublishAt());
    }

    @Test
    void 디자인_최종_공개_후_개발_매칭이_시작되면_개발만_변경할_수_있다() {
        // given
        ProjectMatchingRound designRandom = randomRound();
        designRandom.executeAutoDecision(null);
        designRandom.publishResult(designRandom.getResultPublishAt());
        Instant developerStart = START.plusSeconds(30L * 24 * 3600);
        ProjectMatchingRound developerFirst = ProjectMatchingRound.create("개발자 1차", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.FIRST, 1L, developerStart, developerStart.plusSeconds(3600),
            developerStart.plusSeconds(7200), developerStart.plusSeconds(10800));
        List<ProjectMatchingRound> rounds = List.of(publishedThirdRound(), designRandom, developerFirst);

        // when & then
        assertThat(policy.isChangeAllowedInPeriod(rounds, developerStart.minusNanos(1))).isFalse();
        assertThat(policy.isChangeAllowedInPeriod(rounds, developerStart)).isTrue();
        assertThat(policy.validateChange(rounds, MatchingType.PLAN_DEVELOPER, developerStart)).isEmpty();
        assertBlocked(rounds, developerStart);
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
