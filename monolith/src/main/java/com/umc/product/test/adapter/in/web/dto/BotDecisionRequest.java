package com.umc.product.test.adapter.in.web.dto;

import java.util.Set;

import com.umc.product.test.domain.rehearsal.BotDecisionParams;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * 모든 필드를 생략할 수 있다. fillRate 기본 1차 0.6·2차 0.85·3차 1.0, jitter 기본 0.2.
 */
public record BotDecisionRequest(
    @DecimalMin("0.0") @DecimalMax("1.0") Double fillRate,
    @DecimalMin("0.0") @DecimalMax(value = "1.0", inclusive = false) Double jitter,
    Set<Long> excludeProjectIds,
    Long seed
) {
    public static BotDecisionParams defaults() {
        return BotDecisionParams.defaults();
    }

    public BotDecisionParams toParams() {
        return new BotDecisionParams(fillRate, jitter == null ? BotDecisionParams.DEFAULT_JITTER : jitter,
            excludeProjectIds, seed);
    }
}
