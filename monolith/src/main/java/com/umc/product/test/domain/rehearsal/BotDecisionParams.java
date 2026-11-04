package com.umc.product.test.domain.rehearsal;

import java.util.Set;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * @param fillRate          null이면 1차 0.6, 2차 0.85, 3차 1.0
 * @param jitter            프로젝트마다 고정하는 채움률 흔들림 폭. 0.2면 0.8~1.2배
 * @param excludeProjectIds QA 프로젝트 말고도 봇이 결정하지 않을 프로젝트
 * @param seed              null이면 (지부, 유형, 차수 번호)로 정한 기본 seed
 */
public record BotDecisionParams(Double fillRate, double jitter, Set<Long> excludeProjectIds, Long seed) {
    public static final double DEFAULT_JITTER = 0.2;

    public BotDecisionParams {
        if (fillRate != null && (fillRate < 0 || fillRate > 1)) {
            throw new IllegalArgumentException("fillRate는 0 이상 1 이하여야 합니다: " + fillRate);
        }
        if (jitter < 0 || jitter >= 1) {
            throw new IllegalArgumentException("jitter는 0 이상 1 미만이어야 합니다: " + jitter);
        }
        excludeProjectIds = excludeProjectIds == null ? Set.of() : Set.copyOf(excludeProjectIds);
    }

    public static BotDecisionParams defaults() {
        return new BotDecisionParams(null, DEFAULT_JITTER, Set.of(), null);
    }

    double fillRateFor(MatchingPhase phase) {
        if (fillRate != null) {
            return fillRate;
        }
        return switch (phase) {
            case FIRST -> 0.6;
            case SECOND -> 0.85;
            case THIRD, RANDOM_MATCHING -> 1.0;
        };
    }

    long seedFor(RoundSnapshot.Round round) {
        return seed != null ? seed : SeedHash.defaultSeed(round.chapterName(), round.type(), round.times().phase());
    }
}
