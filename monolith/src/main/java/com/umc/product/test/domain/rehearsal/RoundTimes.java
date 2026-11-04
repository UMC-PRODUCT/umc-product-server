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
}
