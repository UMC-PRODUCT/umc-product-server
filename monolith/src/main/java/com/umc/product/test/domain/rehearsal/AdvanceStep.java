package com.umc.product.test.domain.rehearsal;

import java.util.Optional;

import com.umc.product.project.domain.enums.MatchingPhase;

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

    int mark() {
        return isRandom() ? ordinal() - ADJUSTMENT_START.ordinal() : ordinal();
    }

    private boolean isRandom() {
        return ordinal() >= ADJUSTMENT_START.ordinal();
    }
}
