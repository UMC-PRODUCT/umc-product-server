package com.umc.product.test.domain.rehearsal;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.AlreadyReached;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Refused;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Rewrite;

/**
 * 차수 시각을 {@link AdvanceStep#of(MatchingPhase)}의 순서 있는 표식 목록으로 본다. 넘기는 단계의 표식을 지금으로 당기고,
 * 그 앞 표식은 간격을 지키도록 필요한 만큼만 앞으로 당긴다. 그 뒤 표식과 같은 묶음 뒤 차수의 표식은 내일 같은 시각보다 이르면 내일 이후 슬롯으로 민다. 그보다 늦게 등록된 시각은 앞 표식과의
 * 간격이 맞는 한 그대로 둔다. 원래 시각과 슬롯이 모두 도메인 간격을 지키므로 결과도 지킨다.
 */
public final class AdvancePlanner {

    static final Duration NEXT_DAY = Duration.ofDays(1);
    static final Duration STEP_GAP = Duration.ofHours(1);

    private AdvancePlanner() {
    }

    public static AdvanceDecision plan(MatchingSequence sequence, Long roundId, AdvanceStep step, Instant now) {
        RoundTimes round = sequence.get(roundId);
        Optional<Refused> refusal = refusal(sequence, round, step, now);
        if (refusal.isPresent()) {
            return refusal.get();
        }
        if (reached(round, step, now)) {
            return new AlreadyReached();
        }
        return new Rewrite(rewrite(sequence, round, step, now));
    }

    private static Optional<Refused> refusal(MatchingSequence sequence, RoundTimes round, AdvanceStep step,
        Instant now) {
        if (!step.appliesTo(round.phase())) {
            return refuse(SeedErrorCode.STEP_OUT_OF_ORDER, step + " 단계는 " + round.phase() + " 차수에 없습니다.");
        }
        if (sequence.rounds().stream().anyMatch(each -> each.resultPublishAt() == null)) {
            return refuse(SeedErrorCode.STEP_OUT_OF_ORDER, "발표 시각이 없는 기존 차수가 있는 묶음은 넘길 수 없습니다.");
        }
        if (step == AdvanceStep.APPLICATION_START
            && sequence.previous(round.roundId()).filter(previous -> !previous.published()).isPresent()) {
            return refuse(SeedErrorCode.PREVIOUS_ROUND_NOT_PUBLISHED, null);
        }
        if (step == AdvanceStep.RESULT_PUBLISH && !round.autoDecided()) {
            return refuse(SeedErrorCode.AUTO_DECISION_NOT_EXECUTED, null);
        }
        if (round.phase() == MatchingPhase.RANDOM_MATCHING && !round.autoDecided()) {
            return refuse(SeedErrorCode.RANDOM_NOT_ASSIGNED, null);
        }
        Optional<AdvanceStep> previousStep = step.previous();
        if (previousStep.isPresent() && !reached(round, previousStep.get(), now)) {
            return refuse(SeedErrorCode.STEP_OUT_OF_ORDER,
                step + " 전에 " + previousStep.get() + " 단계로 넘겨야 합니다.");
        }
        return Optional.empty();
    }

    private static Optional<Refused> refuse(SeedErrorCode code, String reason) {
        return Optional.of(new Refused(code, reason));
    }

    private static boolean reached(RoundTimes round, AdvanceStep step, Instant now) {
        return !step.timeOf(round).isAfter(now);
    }

    private static List<RoundTimes> rewrite(MatchingSequence sequence, RoundTimes round, AdvanceStep step,
        Instant now) {
        List<AdvanceStep> steps = AdvanceStep.of(round.phase());
        List<Instant> marks = new ArrayList<>(marks(round));
        int target = steps.indexOf(step);
        marks.set(target, now);
        for (int i = target - 1; i >= 0; i--) {
            Instant latest = marks.get(i + 1).minus(steps.get(i + 1).minGapBefore());
            if (marks.get(i).isAfter(latest)) {
                marks.set(i, latest);
            }
        }
        Slots slots = new Slots(now.plus(NEXT_DAY));
        for (int i = target + 1; i < marks.size(); i++) {
            marks.set(i, slots.place(marks.get(i), steps.get(i).minGapBefore()));
        }

        List<RoundTimes> next = new ArrayList<>();
        next.add(withMarks(round, marks));
        for (RoundTimes later : sequence.after(round.roundId())) {
            List<AdvanceStep> laterSteps = AdvanceStep.of(later.phase());
            List<Instant> laterMarks = new ArrayList<>(marks(later));
            for (int i = 0; i < laterMarks.size(); i++) {
                laterMarks.set(i, slots.place(laterMarks.get(i), laterSteps.get(i).minGapBefore()));
            }
            RoundTimes moved = withMarks(later, laterMarks);
            if (!moved.equals(later)) {
                next.add(moved);
            }
        }
        return next;
    }

    private static List<Instant> marks(RoundTimes round) {
        return AdvanceStep.of(round.phase()).stream().map(step -> step.timeOf(round)).toList();
    }

    private static RoundTimes withMarks(RoundTimes round, List<Instant> marks) {
        if (round.phase() == MatchingPhase.RANDOM_MATCHING) {
            return round.withSchedule(marks.get(0), marks.get(1), marks.get(1), marks.get(2));
        }
        return round.withSchedule(marks.get(0), marks.get(1), marks.get(2), marks.get(3));
    }

    private static final class Slots {

        private final Instant firstSlot;
        private Instant previous;

        private Slots(Instant firstSlot) {
            this.firstSlot = firstSlot;
        }

        private Instant place(Instant original, Duration minGap) {
            Instant placed;
            if (previous == null) {
                placed = original.isBefore(firstSlot) ? firstSlot : original;
            } else {
                placed = original.isBefore(previous.plus(minGap)) ? previous.plus(STEP_GAP) : original;
            }
            previous = placed;
            return placed;
        }
    }
}
