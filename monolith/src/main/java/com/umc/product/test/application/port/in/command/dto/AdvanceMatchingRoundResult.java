package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;
import com.umc.product.test.domain.rehearsal.AdvanceStep;
import com.umc.product.test.domain.rehearsal.RoundSummary;
import com.umc.product.test.domain.rehearsal.RoundTimeChange;

/**
 * 차수 넘기기 결과. timers는 같은 지부·유형 차수 전부의 다시 맞춘 알람 시각, summary는 그 뒤 넘긴 차수의 요약이다.
 */
public record AdvanceMatchingRoundResult(
    Long roundId,
    AdvanceStep step,
    Outcome outcome,
    List<RoundTimeChange> timeChanges,
    List<MatchingRoundTimers> timers,
    RoundSummary summary
) {

    public enum Outcome {
        APPLIED,
        ALREADY_REACHED
    }
}
