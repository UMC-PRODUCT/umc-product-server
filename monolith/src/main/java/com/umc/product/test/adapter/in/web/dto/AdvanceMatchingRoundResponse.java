package com.umc.product.test.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;
import com.umc.product.test.application.port.in.command.dto.AdvanceMatchingRoundResult;
import com.umc.product.test.application.port.in.command.dto.AdvanceMatchingRoundResult.Outcome;
import com.umc.product.test.domain.rehearsal.AdvanceStep;
import com.umc.product.test.domain.rehearsal.RoundTimeChange;

public record AdvanceMatchingRoundResponse(
    Long roundId,
    AdvanceStep step,
    Outcome outcome,
    List<TimeChange> timeChanges,
    List<Timer> timers,
    RoundSummaryResponse summary
) {

    public static AdvanceMatchingRoundResponse from(AdvanceMatchingRoundResult result) {
        return new AdvanceMatchingRoundResponse(
            result.roundId(),
            result.step(),
            result.outcome(),
            result.timeChanges().stream().map(TimeChange::from).toList(),
            result.timers().stream().map(Timer::from).toList(),
            RoundSummaryResponse.from(result.summary())
        );
    }

    public record TimeChange(Long roundId, String field, Instant before, Instant after) {

        static TimeChange from(RoundTimeChange change) {
            return new TimeChange(change.roundId(), change.field(), change.before(), change.after());
        }
    }

    /** 등록하지 않은 알람은 null이다. */
    public record Timer(Long roundId, Instant autoDecideAt, Instant publishAt) {

        static Timer from(MatchingRoundTimers timers) {
            return new Timer(timers.roundId(), timers.autoDecideAt(), timers.publishAt());
        }
    }
}
