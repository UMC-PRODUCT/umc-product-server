package com.umc.product.test.domain.rehearsal;

import java.time.Instant;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 매칭 차수 하나의 시각과 실행 기록. 랜덤 매칭은 endsAt과 decisionDeadline이 같다.
 */
public record RoundTimes(
    Long roundId, MatchingPhase phase,
    Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt,
    Instant autoDecisionExecutedAt, Instant resultPublishedAt
) {

    public boolean autoDecided() {
        return autoDecisionExecutedAt != null;
    }

    public boolean published() {
        return resultPublishedAt != null;
    }

    public RoundTimes withSchedule(Instant startsAt, Instant endsAt, Instant decisionDeadline, Instant resultPublishAt) {
        return new RoundTimes(roundId, phase, startsAt, endsAt, decisionDeadline, resultPublishAt,
            autoDecisionExecutedAt, resultPublishedAt);
    }

    /** 운영 규칙({@code ProjectMatchingRound.isOpenAt})과 같다. */
    public boolean isApplicationOpenAt(Instant now) {
        return phase != MatchingPhase.RANDOM_MATCHING && !now.isBefore(startsAt) && !now.isAfter(endsAt);
    }

    /**
     * 운영 규칙({@code ProjectMatchingRound.validateIsMutableAt})보다 엄격하다. 랜덤 매칭과 자동 선발이 끝난 차수도
     * 닫힌 것으로 본다.
     */
    public boolean isDecisionOpenAt(Instant now) {
        return phase != MatchingPhase.RANDOM_MATCHING && now.isAfter(endsAt) && !now.isAfter(decisionDeadline)
            && !autoDecided();
    }

    public RoundStage stageAt(Instant now) {
        if (published()) {
            return RoundStage.PUBLISHED;
        }
        if (phase == MatchingPhase.RANDOM_MATCHING) {
            if (!autoDecided()) {
                return RoundStage.AWAITING_ASSIGNMENT;
            }
            if (now.isBefore(startsAt)) {
                return RoundStage.ASSIGNED;
            }
            return now.isBefore(endsAt) ? RoundStage.ADJUSTMENT_OPEN : RoundStage.ADJUSTMENT_CLOSED;
        }
        if (now.isBefore(startsAt)) {
            return RoundStage.BEFORE_START;
        }
        if (!now.isAfter(endsAt)) {
            return RoundStage.APPLICATION_OPEN;
        }
        if (!now.isAfter(decisionDeadline) && !autoDecided()) {
            return RoundStage.DECISION_WINDOW;
        }
        return autoDecided() ? RoundStage.AWAITING_PUBLISH : RoundStage.AWAITING_AUTO_DECISION;
    }
}
