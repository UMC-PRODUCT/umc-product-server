package com.umc.product.test.application.port.in.command.dto;

import java.util.Map;

import com.umc.product.test.domain.rehearsal.RoundSummary;
import com.umc.product.test.domain.rehearsal.SkipReason;

/**
 * @param processed 이번 호출에 성공한 지원·결정 수
 * @param failed    운영 에러 코드별 실패 수. 다시 부르면 실패한 것만 다시 시도한다
 */
public record BotRunResult(
    Outcome outcome,
    int processed,
    Map<SkipReason, Integer> skipped,
    Map<String, Integer> failed,
    RoundSummary summary
) {
    public BotRunResult {
        skipped = Map.copyOf(skipped);
        failed = Map.copyOf(failed);
    }

    public enum Outcome {
        APPLIED,
        UNCHANGED
    }
}
