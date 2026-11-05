package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Objects;

import com.umc.product.test.domain.exception.SeedErrorCode;

public sealed interface AdvanceDecision {

    record Refused(SeedErrorCode code, String reason) implements AdvanceDecision {
        public Refused {
            Objects.requireNonNull(reason, "거부 사유가 필요합니다.");
        }
    }

    record AlreadyReached() implements AdvanceDecision {
    }

    record Rewrite(List<RoundTimes> next) implements AdvanceDecision {
    }
}
