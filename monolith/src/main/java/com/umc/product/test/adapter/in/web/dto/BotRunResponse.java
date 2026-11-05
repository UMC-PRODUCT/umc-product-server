package com.umc.product.test.adapter.in.web.dto;

import java.util.Map;

import com.umc.product.test.application.port.in.command.dto.BotRunResult;
import com.umc.product.test.application.port.in.command.dto.BotRunResult.Outcome;
import com.umc.product.test.domain.rehearsal.SkipReason;

public record BotRunResponse(
    Outcome outcome,
    int processed,
    Map<SkipReason, Integer> skipped,
    Map<String, Integer> failed,
    RoundSummaryResponse summary
) {
    public static BotRunResponse from(BotRunResult result) {
        return new BotRunResponse(result.outcome(), result.processed(), result.skipped(), result.failed(),
            RoundSummaryResponse.from(result.summary()));
    }
}
