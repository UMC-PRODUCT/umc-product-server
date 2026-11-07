package com.umc.product.test.domain.rehearsal;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 차수 시각을 순서 있는 단계 표로 본다. 1~3차는 [시작, 지원 마감, 선발 마감, 발표], 랜덤은 [조정 시작, 조정 종료(= 선발 마감), 최종 공개]다.
 * {@code minGapBefore}는 같은 차수 앞 단계와의 도메인 최소 간격이고, 차수의 첫 단계는 앞 차수 발표와의 간격(0)이다.
 */
public enum AdvanceStep {
    APPLICATION_START(Track.REGULAR, Duration.ZERO, RoundTimes::startsAt),
    APPLICATION_CLOSE(Track.REGULAR, Duration.ofSeconds(1), RoundTimes::endsAt),
    DECISION_DEADLINE(Track.REGULAR, Duration.ofSeconds(1), RoundTimes::decisionDeadline),
    RESULT_PUBLISH(Track.REGULAR, Duration.ofMinutes(5), RoundTimes::resultPublishAt),
    ADJUSTMENT_START(Track.RANDOM, Duration.ZERO, RoundTimes::startsAt),
    ADJUSTMENT_END(Track.RANDOM, Duration.ofSeconds(1), RoundTimes::endsAt),
    FINAL_PUBLISH(Track.RANDOM, Duration.ofMinutes(5), RoundTimes::resultPublishAt);

    private final Track track;
    private final Duration minGapBefore;
    private final Function<RoundTimes, Instant> time;

    AdvanceStep(Track track, Duration minGapBefore, Function<RoundTimes, Instant> time) {
        this.track = track;
        this.minGapBefore = minGapBefore;
        this.time = time;
    }

    static List<AdvanceStep> of(MatchingPhase phase) {
        return of(Track.of(phase));
    }

    private static List<AdvanceStep> of(Track track) {
        return Arrays.stream(values()).filter(step -> step.track == track).toList();
    }

    public boolean appliesTo(MatchingPhase phase) {
        return track == Track.of(phase);
    }

    public Optional<AdvanceStep> previous() {
        List<AdvanceStep> steps = of(track);
        int index = steps.indexOf(this);
        return index == 0 ? Optional.empty() : Optional.of(steps.get(index - 1));
    }

    Duration minGapBefore() {
        return minGapBefore;
    }

    Instant timeOf(RoundTimes round) {
        return time.apply(round);
    }

    private enum Track {
        REGULAR,
        RANDOM;

        private static Track of(MatchingPhase phase) {
            return phase == MatchingPhase.RANDOM_MATCHING ? RANDOM : REGULAR;
        }
    }
}
