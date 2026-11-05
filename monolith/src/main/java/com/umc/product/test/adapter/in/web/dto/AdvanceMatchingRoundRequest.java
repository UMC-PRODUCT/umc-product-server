package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.domain.rehearsal.AdvanceStep;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AdvanceMatchingRoundRequest(
    @Schema(description = "1·2·3차: APPLICATION_START, APPLICATION_CLOSE, DECISION_DEADLINE, RESULT_PUBLISH. "
        + "랜덤: ADJUSTMENT_START, ADJUSTMENT_END, FINAL_PUBLISH")
    @NotNull AdvanceStep step
) {
}
