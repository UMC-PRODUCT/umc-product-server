package com.umc.product.test.domain.rehearsal;

import java.util.List;

import com.umc.product.test.domain.exception.SeedErrorCode;

public sealed interface AdvanceDecision {

    record Refused(SeedErrorCode code, String reason) implements AdvanceDecision {
    }

    record AlreadyReached() implements AdvanceDecision {
    }

    record Rewrite(List<RoundTimes> next) implements AdvanceDecision {
    }
}
