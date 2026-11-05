package com.umc.product.test.domain.rehearsal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.AlreadyReached;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Refused;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Rewrite;

class AdvancePlannerTest {

    private static final Instant NOW = Instant.parse("2026-11-04T10:00:00Z");
    private static final Instant TOMORROW = NOW.plus(Duration.ofDays(1));
    private static final Duration MIN_PHASE_INTERVAL = Duration.ofMinutes(1);
    private static final long FIRST = 11L;
    private static final long SECOND = 12L;
    private static final long THIRD = 13L;
    private static final long RANDOM = 14L;
    private static final List<Long> ROUND_IDS = List.of(FIRST, SECOND, THIRD, RANDOM);

    @ParameterizedTest(name = "{0}")
    @MethodSource("registrations")
    @DisplayName("리허설 전체를 단계마다 넘기면 매번 도메인 시각 규칙을 지키고, 같은 단계를 다시 넘기면 이미 도달로 끝난다")
    void walksWholeRehearsalKeepingDomainInvariants(String name, MatchingSequence registered) {
        Rehearsal rehearsal = new Rehearsal(registered, NOW);

        rehearsal.walk();

        assertThat(rehearsal.sequence.rounds()).allMatch(RoundTimes::published);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("registrations")
    @DisplayName("리허설 중 어느 상태에서 어느 차수·단계를 골라도 다시 쓰는 것은 다음 한 단계뿐이고, 그 결과는 도메인 시각 규칙을 지킨다")
    void onlyNextStepRewritesFromEveryReachableState(String name, MatchingSequence registered) {
        Rehearsal rehearsal = new Rehearsal(registered, NOW);
        rehearsal.walk();

        int rewrites = 0;
        for (State state : rehearsal.statesIncludingStart()) {
            int rewritesInState = 0;
            for (Long roundId : ROUND_IDS) {
                for (AdvanceStep step : AdvanceStep.values()) {
                    AdvanceDecision decision = AdvancePlanner.plan(state.sequence(), roundId, step, state.now());
                    if (decision instanceof Rewrite rewrite) {
                        assertRewriteProperties(state, roundId, step, rewrite);
                        rewritesInState++;
                    }
                }
            }
            assertThat(rewritesInState).as("rewrites at %s", state.now()).isLessThanOrEqualTo(1);
            rewrites += rewritesInState;
        }
        assertThat(rewrites).isEqualTo(rehearsal.advances);
    }

    static Stream<Arguments> registrations() {
        Instant soon = NOW.plus(Duration.ofHours(2));
        Instant far = NOW.plus(Duration.ofDays(3));
        MatchingSequence farRegistered = registered(far);
        return Stream.of(
            Arguments.of("모든 차수를 사흘 뒤부터 등록", farRegistered),
            Arguments.of("모든 차수를 오늘 몇 시간 안에 몰아 등록", cramped(soon)),
            Arguments.of("1차만 오늘, 나머지는 사흘 뒤부터 등록", sequence(
                cramped(soon).get(FIRST), farRegistered.get(SECOND), farRegistered.get(THIRD),
                farRegistered.get(RANDOM))));
    }

    @Test
    @DisplayName("1차 지원 시작은 시작을 지금으로, 내일보다 이른 뒤 단계와 뒤 차수 시각을 내일부터 한 시간 간격 슬롯으로 민다")
    void applicationStartPushesEarlierTimesIntoTomorrowSlots() {
        // given
        MatchingSequence sequence = cramped(NOW.plus(Duration.ofHours(2)));

        // when
        Rewrite rewrite = (Rewrite) AdvancePlanner.plan(sequence, FIRST, AdvanceStep.APPLICATION_START, NOW);

        // then
        Map<Long, RoundTimes> next = byId(rewrite.next());
        assertThat(next.get(FIRST)).isEqualTo(regular(FIRST, MatchingPhase.FIRST,
            NOW, TOMORROW, hours(TOMORROW, 1), hours(TOMORROW, 2)));
        assertThat(next.get(SECOND)).isEqualTo(regular(SECOND, MatchingPhase.SECOND,
            hours(TOMORROW, 3), hours(TOMORROW, 4), hours(TOMORROW, 5), hours(TOMORROW, 6)));
        assertThat(next.get(RANDOM)).isEqualTo(random(RANDOM,
            hours(TOMORROW, 11), hours(TOMORROW, 12), hours(TOMORROW, 13)));
        assertValid(apply(sequence, rewrite));
    }

    @Test
    @DisplayName("QA가 내일 이후로 등록한 시각은 앞 시각과 간격이 맞으면 그대로 둔다")
    void keepsFutureTimesRegisteredByQa() {
        // given
        Instant base = NOW.plus(Duration.ofDays(3));
        MatchingSequence sequence = registered(base);
        RoundTimes first = sequence.get(FIRST);

        // when
        Rewrite rewrite = (Rewrite) AdvancePlanner.plan(sequence, FIRST, AdvanceStep.APPLICATION_START, NOW);

        // then
        assertThat(rewrite.next()).containsExactly(first.withSchedule(
            NOW, first.endsAt(), first.decisionDeadline(), first.resultPublishAt()));
    }

    @Test
    @DisplayName("내일 이후 시각이라도 민 앞 시각과 간격이 모자라면 앞 시각 한 시간 뒤로 민다")
    void pushesFutureTimeWhenPushedPredecessorLeavesNoGap() {
        // given
        MatchingSequence sequence = sequence(
            regular(FIRST, MatchingPhase.FIRST, hours(NOW, 1), hours(NOW, 2), hours(NOW, 3),
                TOMORROW.plus(Duration.ofMinutes(2))));

        // when
        Rewrite rewrite = (Rewrite) AdvancePlanner.plan(sequence, FIRST, AdvanceStep.APPLICATION_START, NOW);

        // then
        assertThat(rewrite.next()).containsExactly(regular(FIRST, MatchingPhase.FIRST,
            NOW, TOMORROW, hours(TOMORROW, 1), hours(TOMORROW, 2)));
    }

    @Test
    @DisplayName("발표로 넘기면 발표를 지금으로, 선발 마감을 5분 이상 앞으로 당긴다")
    void resultPublishPullsDecisionDeadlineFiveMinutesBack() {
        // given
        Instant deadline = NOW.minus(Duration.ofMinutes(2));
        RoundTimes first = regular(FIRST, MatchingPhase.FIRST, deadline.minus(Duration.ofHours(2)),
            deadline.minusSeconds(30), deadline, TOMORROW);
        MatchingSequence sequence = sequence(autoDecided(first, NOW.minus(Duration.ofMinutes(1))));

        // when
        Rewrite rewrite = (Rewrite) AdvancePlanner.plan(sequence, FIRST, AdvanceStep.RESULT_PUBLISH, NOW);

        // then
        RoundTimes next = rewrite.next().getFirst();
        assertThat(next.resultPublishAt()).isEqualTo(NOW);
        assertThat(next.decisionDeadline()).isEqualTo(NOW.minus(Duration.ofMinutes(5)));
        assertThat(next.endsAt()).isEqualTo(NOW.minus(Duration.ofMinutes(5)).minusSeconds(1));
        assertThat(next.startsAt()).isEqualTo(first.startsAt());
        assertValid(sequence(next));
    }

    @Test
    @DisplayName("3차를 발표로 넘기면 랜덤 차수 발표 시각을 내일 이후로 둬 랜덤 배정보다 먼저 돌지 않게 한다")
    void thirdPublishKeepsRandomPublicationInFuture() {
        // given
        Instant deadline = NOW.minus(Duration.ofMinutes(10));
        MatchingSequence sequence = sequence(
            published(regular(FIRST, MatchingPhase.FIRST, days(NOW, -9), days(NOW, -8), hours(days(NOW, -8), 1),
                hours(days(NOW, -8), 2))),
            published(regular(SECOND, MatchingPhase.SECOND, days(NOW, -6), days(NOW, -5), hours(days(NOW, -5), 1),
                hours(days(NOW, -5), 2))),
            autoDecided(regular(THIRD, MatchingPhase.THIRD, hours(deadline, -3), hours(deadline, -1), deadline,
                hours(NOW, 1)), NOW.minus(Duration.ofMinutes(9))),
            random(RANDOM, hours(NOW, 2), hours(NOW, 3), hours(NOW, 4)));

        // when
        Rewrite rewrite = (Rewrite) AdvancePlanner.plan(sequence, THIRD, AdvanceStep.RESULT_PUBLISH, NOW);

        // then
        RoundTimes randomRound = byId(rewrite.next()).get(RANDOM);
        assertThat(randomRound).isEqualTo(random(RANDOM, TOMORROW, hours(TOMORROW, 1), hours(TOMORROW, 2)));
        assertValid(apply(sequence, rewrite));
    }

    @Test
    @DisplayName("랜덤 조정 시작·종료·최종 공개는 각 시각을 지금으로 두고 조정 종료와 선발 마감을 같게 유지한다")
    void randomStepsKeepEndsAtEqualToDecisionDeadline() {
        // given
        MatchingSequence sequence = sequence(
            published(regular(THIRD, MatchingPhase.THIRD, days(NOW, -3), days(NOW, -2), hours(days(NOW, -2), 1),
                hours(days(NOW, -2), 2))),
            autoDecided(random(RANDOM, TOMORROW, hours(TOMORROW, 1), hours(TOMORROW, 2)), NOW.minusSeconds(60)));

        // when
        MatchingSequence started = apply(sequence,
            (Rewrite) AdvancePlanner.plan(sequence, RANDOM, AdvanceStep.ADJUSTMENT_START, NOW));
        Instant later = NOW.plus(Duration.ofMinutes(30));
        MatchingSequence ended = apply(started,
            (Rewrite) AdvancePlanner.plan(started, RANDOM, AdvanceStep.ADJUSTMENT_END, later));
        Instant last = later.plus(Duration.ofMinutes(1));
        MatchingSequence finalized = apply(ended,
            (Rewrite) AdvancePlanner.plan(ended, RANDOM, AdvanceStep.FINAL_PUBLISH, last));

        // then
        assertThat(started.get(RANDOM).startsAt()).isEqualTo(NOW);
        assertThat(ended.get(RANDOM).endsAt()).isEqualTo(later);
        assertThat(ended.get(RANDOM).resultPublishAt()).isEqualTo(hours(TOMORROW, 2));
        RoundTimes result = finalized.get(RANDOM);
        assertThat(result.resultPublishAt()).isEqualTo(last);
        assertThat(result.endsAt()).isEqualTo(last.minus(Duration.ofMinutes(5)));
        assertThat(result.decisionDeadline()).isEqualTo(result.endsAt());
        assertThat(result.startsAt()).isBefore(result.endsAt());
        List.of(started, ended, finalized).forEach(AdvancePlannerTest::assertValid);
    }

    @Test
    @DisplayName("앞 차수 발표 전 다음 차수 시작, 자동 선발 전 발표, 단계 건너뛰기, 랜덤 배정 전 조정 시작을 거부한다")
    void refusesOutOfOrderSteps() {
        MatchingSequence sequence = registered(NOW.plus(Duration.ofDays(3)));
        MatchingSequence afterDeadline = sequence(
            regular(FIRST, MatchingPhase.FIRST, hours(NOW, -3), hours(NOW, -2), NOW.minusSeconds(30), TOMORROW),
            sequence.get(SECOND), sequence.get(THIRD), sequence.get(RANDOM));

        assertThat(refusal(sequence, SECOND, AdvanceStep.APPLICATION_START))
            .isEqualTo(SeedErrorCode.PREVIOUS_ROUND_NOT_PUBLISHED);
        assertThat(refusal(afterDeadline, FIRST, AdvanceStep.RESULT_PUBLISH))
            .isEqualTo(SeedErrorCode.AUTO_DECISION_NOT_EXECUTED);
        assertThat(refusal(sequence, FIRST, AdvanceStep.APPLICATION_CLOSE))
            .isEqualTo(SeedErrorCode.STEP_OUT_OF_ORDER);
        assertThat(refusal(sequence, FIRST, AdvanceStep.ADJUSTMENT_START))
            .isEqualTo(SeedErrorCode.STEP_OUT_OF_ORDER);
        assertThat(refusal(sequence, RANDOM, AdvanceStep.APPLICATION_START))
            .isEqualTo(SeedErrorCode.STEP_OUT_OF_ORDER);
        assertThat(refusal(sequence, RANDOM, AdvanceStep.ADJUSTMENT_START))
            .isEqualTo(SeedErrorCode.RANDOM_NOT_ASSIGNED);
    }

    @Test
    @DisplayName("이미 지난 단계를 다시 넘기면 시각을 바꾸지 않는다")
    void sameStepTwiceIsAlreadyReached() {
        MatchingSequence sequence = registered(NOW.plus(Duration.ofDays(3)));
        MatchingSequence started = apply(sequence,
            (Rewrite) AdvancePlanner.plan(sequence, FIRST, AdvanceStep.APPLICATION_START, NOW));

        assertThat(AdvancePlanner.plan(started, FIRST, AdvanceStep.APPLICATION_START, NOW.plusSeconds(5)))
            .isInstanceOf(AlreadyReached.class);
    }

    @Test
    @DisplayName("발표 시각이 없는 기존 차수가 섞인 묶음은 넘기지 않는다")
    void refusesLegacyRoundsWithoutPublishTime() {
        MatchingSequence sequence = sequence(
            regular(FIRST, MatchingPhase.FIRST, hours(NOW, 1), hours(NOW, 2), hours(NOW, 3), null));

        assertThat(refusal(sequence, FIRST, AdvanceStep.APPLICATION_START))
            .isEqualTo(SeedErrorCode.STEP_OUT_OF_ORDER);
    }

    private static void assertRewriteProperties(State state, Long roundId, AdvanceStep step, Rewrite rewrite) {
        MatchingSequence before = state.sequence();
        Instant now = state.now();
        MatchingSequence after = apply(before, rewrite);
        String context = step + " on " + roundId + " at " + now;

        assertValid(after);
        RoundTimes round = after.get(roundId);
        List<Instant> marks = marks(round);
        int target = AdvanceStep.of(round.phase()).indexOf(step);
        assertThat(marks.get(target)).as(context).isEqualTo(now);
        for (int i = 0; i < target; i++) {
            assertThat(marks.get(i)).as(context).isBeforeOrEqualTo(marks(before.get(roundId)).get(i));
        }
        for (int i = target + 1; i < marks.size(); i++) {
            assertThat(marks.get(i)).as(context).isAfterOrEqualTo(now.plus(Duration.ofDays(1)));
        }
        for (RoundTimes later : after.after(roundId)) {
            assertThat(marks(later)).as(context).allMatch(mark -> !mark.isBefore(now.plus(Duration.ofDays(1))));
        }
        for (Long other : ROUND_IDS) {
            RoundTimes original = before.get(other);
            RoundTimes changed = after.get(other);
            assertThat(changed.autoDecisionExecutedAt()).isEqualTo(original.autoDecisionExecutedAt());
            assertThat(changed.resultPublishedAt()).isEqualTo(original.resultPublishedAt());
            if (before.rounds().indexOf(original) < before.rounds().indexOf(before.get(roundId))) {
                assertThat(changed).as(context).isEqualTo(original);
            }
        }
        assertThat(AdvancePlanner.plan(after, roundId, step, now)).as(context).isInstanceOf(AlreadyReached.class);
    }

    private static SeedErrorCode refusal(MatchingSequence sequence, Long roundId, AdvanceStep step) {
        AdvanceDecision decision = AdvancePlanner.plan(sequence, roundId, step, NOW);
        assertThat(decision).isInstanceOf(Refused.class);
        return ((Refused) decision).code();
    }

    /** 운영 차수 생성이 쓰는 엔티티 검증과 차수 순서 규칙(ProjectMatchingRoundCommandService.validatePhaseSequence). */
    static void assertValid(MatchingSequence sequence) {
        List<RoundTimes> rounds = sequence.rounds();
        for (RoundTimes round : rounds) {
            assertThatCode(() -> ProjectMatchingRound.validateSchedule(sequence.type(), round.phase(),
                round.startsAt(), round.endsAt(), round.decisionDeadline(), round.resultPublishAt()))
                .as("%s", round)
                .doesNotThrowAnyException();
        }
        for (int i = 0; i < rounds.size(); i++) {
            for (int j = i + 1; j < rounds.size(); j++) {
                RoundTimes earlier = rounds.get(i);
                RoundTimes later = rounds.get(j);
                assertThat(later.startsAt()).as("%s after %s", later, earlier)
                    .isAfterOrEqualTo(earlier.decisionDeadline().plus(MIN_PHASE_INTERVAL))
                    .isAfterOrEqualTo(earlier.resultPublishAt());
            }
        }
    }

    private static final class Rehearsal {

        private final State start;
        private final List<State> states = new ArrayList<>();
        private int advances;
        private MatchingSequence sequence;
        private Instant now;

        private Rehearsal(MatchingSequence sequence, Instant now) {
            this.start = new State(sequence, now);
            this.sequence = sequence;
            this.now = now;
        }

        private List<State> statesIncludingStart() {
            List<State> all = new ArrayList<>();
            all.add(start);
            all.addAll(states);
            return all;
        }

        private void walk() {
            for (Long roundId : List.of(FIRST, SECOND, THIRD)) {
                advance(roundId, AdvanceStep.APPLICATION_START);
                advance(roundId, AdvanceStep.APPLICATION_CLOSE);
                advance(roundId, AdvanceStep.DECISION_DEADLINE);
                assertThat(AdvancePlanner.plan(sequence, roundId, AdvanceStep.RESULT_PUBLISH, now))
                    .isInstanceOfSatisfying(Refused.class, refused ->
                        assertThat(refused.code()).isEqualTo(SeedErrorCode.AUTO_DECISION_NOT_EXECUTED));
                tick(Duration.ofMinutes(1).plusSeconds(1));
                autoDecide(roundId);
                advance(roundId, AdvanceStep.RESULT_PUBLISH);
                publish(roundId);
            }
            assertThat(sequence.get(RANDOM).resultPublishAt()).isAfter(now);
            tick(Duration.ofMinutes(1).plusSeconds(1));
            autoDecide(RANDOM);
            advance(RANDOM, AdvanceStep.ADJUSTMENT_START);
            advance(RANDOM, AdvanceStep.ADJUSTMENT_END);
            advance(RANDOM, AdvanceStep.FINAL_PUBLISH);
            publish(RANDOM);
        }

        private void advance(Long roundId, AdvanceStep step) {
            tick(Duration.ofMinutes(3));
            AdvanceDecision decision = AdvancePlanner.plan(sequence, roundId, step, now);
            assertThat(decision).as(step + " on " + roundId).isInstanceOf(Rewrite.class);
            assertRewriteProperties(new State(sequence, now), roundId, step, (Rewrite) decision);
            sequence = apply(sequence, (Rewrite) decision);
            advances++;
            record();
            tick(Duration.ofSeconds(1));
            assertThat(AdvancePlanner.plan(sequence, roundId, step, now)).isInstanceOf(AlreadyReached.class);
        }

        private void autoDecide(Long roundId) {
            RoundTimes round = sequence.get(roundId);
            if (round.phase() == MatchingPhase.RANDOM_MATCHING) {
                assertThat(sequence.get(THIRD).published()).isTrue();
            } else {
                assertThat(round.decisionDeadline()).isBefore(now);
            }
            sequence = replace(sequence, autoDecided(round, now));
            record();
        }

        private void publish(Long roundId) {
            RoundTimes round = sequence.get(roundId);
            assertThat(round.autoDecided()).isTrue();
            assertThat(round.resultPublishAt()).isBeforeOrEqualTo(now);
            sequence = replace(sequence, published(round));
            record();
        }

        private void tick(Duration duration) {
            now = now.plus(duration);
        }

        private void record() {
            states.add(new State(sequence, now));
        }
    }

    private record State(MatchingSequence sequence, Instant now) {
    }

    private static MatchingSequence registered(Instant base) {
        return sequence(
            regular(FIRST, MatchingPhase.FIRST, base, days(base, 1), hours(days(base, 1), 2), hours(days(base, 1), 3)),
            regular(SECOND, MatchingPhase.SECOND, days(base, 2), days(base, 3), hours(days(base, 3), 2),
                hours(days(base, 3), 3)),
            regular(THIRD, MatchingPhase.THIRD, days(base, 4), days(base, 5), hours(days(base, 5), 2),
                hours(days(base, 5), 3)),
            random(RANDOM, days(base, 6), days(base, 7), hours(days(base, 7), 1)));
    }

    private static MatchingSequence cramped(Instant base) {
        return sequence(
            regular(FIRST, MatchingPhase.FIRST, base, hours(base, 1), hours(base, 2), hours(base, 3)),
            regular(SECOND, MatchingPhase.SECOND, hours(base, 4), hours(base, 5), hours(base, 6), hours(base, 7)),
            regular(THIRD, MatchingPhase.THIRD, hours(base, 8), hours(base, 9), hours(base, 10), hours(base, 11)),
            random(RANDOM, hours(base, 12), hours(base, 13), hours(base, 14)));
    }

    private static MatchingSequence sequence(RoundTimes... rounds) {
        return new MatchingSequence(MatchingType.PLAN_DEVELOPER, 1L, List.of(rounds));
    }

    private static MatchingSequence apply(MatchingSequence sequence, Rewrite rewrite) {
        MatchingSequence result = sequence;
        for (RoundTimes round : rewrite.next()) {
            result = replace(result, round);
        }
        return result;
    }

    private static MatchingSequence replace(MatchingSequence sequence, RoundTimes round) {
        return new MatchingSequence(sequence.type(), sequence.chapterId(), sequence.rounds().stream()
            .map(each -> each.roundId().equals(round.roundId()) ? round : each)
            .toList());
    }

    private static Map<Long, RoundTimes> byId(List<RoundTimes> rounds) {
        return rounds.stream().collect(Collectors.toMap(RoundTimes::roundId, Function.identity()));
    }

    private static List<Instant> marks(RoundTimes round) {
        return AdvanceStep.of(round.phase()).stream().map(step -> step.timeOf(round)).toList();
    }

    private static RoundTimes regular(Long id, MatchingPhase phase, Instant startsAt, Instant endsAt,
        Instant decisionDeadline, Instant resultPublishAt) {
        return new RoundTimes(id, phase, startsAt, endsAt, decisionDeadline, resultPublishAt, null, null);
    }

    private static RoundTimes random(Long id, Instant startsAt, Instant endsAt, Instant resultPublishAt) {
        return new RoundTimes(id, MatchingPhase.RANDOM_MATCHING, startsAt, endsAt, endsAt, resultPublishAt, null,
            null);
    }

    private static RoundTimes autoDecided(RoundTimes round, Instant at) {
        return new RoundTimes(round.roundId(), round.phase(), round.startsAt(), round.endsAt(),
            round.decisionDeadline(), round.resultPublishAt(), at, round.resultPublishedAt());
    }

    private static RoundTimes published(RoundTimes round) {
        Instant at = round.resultPublishAt();
        Instant decidedAt = round.autoDecided() ? round.autoDecisionExecutedAt() : round.decisionDeadline();
        return new RoundTimes(round.roundId(), round.phase(), round.startsAt(), round.endsAt(),
            round.decisionDeadline(), round.resultPublishAt(), decidedAt, at);
    }

    private static Instant hours(Instant instant, long hours) {
        return instant.plus(Duration.ofHours(hours));
    }

    private static Instant days(Instant instant, long days) {
        return instant.plus(Duration.ofDays(days));
    }
}
