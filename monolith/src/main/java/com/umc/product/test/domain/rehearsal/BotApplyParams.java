package com.umc.product.test.domain.rehearsal;

import java.util.Set;

/**
 * @param applyRate        차수의 고정 대상 중 지원할 비율
 * @param seed             null이면 (지부, 유형, 차수 번호)로 정한 기본 seed
 */
public record BotApplyParams(double applyRate, Set<Long> excludeMemberIds, Long seed) {
    public static final double DEFAULT_APPLY_RATE = 0.8;

    public BotApplyParams {
        if (applyRate < 0 || applyRate > 1) {
            throw new IllegalArgumentException("applyRate는 0 이상 1 이하여야 합니다: " + applyRate);
        }
        excludeMemberIds = excludeMemberIds == null ? Set.of() : Set.copyOf(excludeMemberIds);
    }

    public static BotApplyParams defaults() {
        return new BotApplyParams(DEFAULT_APPLY_RATE, Set.of(), null);
    }

    long seedFor(RoundSnapshot.Round round) {
        return seed != null ? seed : SeedHash.defaultSeed(round.chapterName(), round.type(), round.phase());
    }
}
