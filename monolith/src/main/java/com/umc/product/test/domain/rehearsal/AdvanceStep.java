package com.umc.product.test.domain.rehearsal;

import java.util.Optional;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 차수 넘기기 단계. 1~3차는 시작, 지원 마감, 선발 마감, 발표 순이고 랜덤 매칭은 조정 시작, 조정 종료, 최종 공개 순이다.
 * 같은 묶음 안의 순서가 그 차수 시각의 순서와 같다.
 */
public enum AdvanceStep {
    APPLICATION_START,
    APPLICATION_CLOSE,
    DECISION_DEADLINE,
    RESULT_PUBLISH,
    ADJUSTMENT_START,
    ADJUSTMENT_END,
    FINAL_PUBLISH;

    public boolean appliesTo(MatchingPhase phase) {
        return isRandom() == (phase == MatchingPhase.RANDOM_MATCHING);
    }

    public Optional<AdvanceStep> previous() {
        if (this == APPLICATION_START || this == ADJUSTMENT_START) {
            return Optional.empty();
        }
        return Optional.of(values()[ordinal() - 1]);
    }

    /** 이 단계가 지금으로 당기는 시각의 차수 안 순번. 0이 시작 시각이다. */
    int mark() {
        return isRandom() ? ordinal() - ADJUSTMENT_START.ordinal() : ordinal();
    }

    private boolean isRandom() {
        return ordinal() >= ADJUSTMENT_START.ordinal();
    }
}
