package com.umc.product.test.adapter.in.web.dto;

import java.util.Set;

import com.umc.product.test.domain.rehearsal.BotApplyParams;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * 모든 필드를 생략할 수 있다. applyRate 기본 0.8, seed 기본은 (지부, 유형, 차수 번호)로 정한 값.
 */
public record BotApplyRequest(
    @DecimalMin("0.0") @DecimalMax("1.0") Double applyRate,
    Set<Long> excludeMemberIds,
    Long seed
) {
    public static BotApplyParams defaults() {
        return BotApplyParams.defaults();
    }

    public BotApplyParams toParams() {
        return new BotApplyParams(applyRate == null ? BotApplyParams.DEFAULT_APPLY_RATE : applyRate,
            excludeMemberIds, seed);
    }
}
