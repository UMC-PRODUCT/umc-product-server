package com.umc.product.test.domain.rehearsal;

import java.util.List;

import com.umc.product.test.domain.exception.SeedErrorCode;

public sealed interface AdvanceDecision {

    record Refused(SeedErrorCode code, String reason) implements AdvanceDecision {
    }

    /** 이미 그 단계 시각이 지났다. 시각은 그대로 두고 알람만 다시 맞춘다. */
    record AlreadyReached() implements AdvanceDecision {
    }

    /** 시각이 바뀌는 차수만 담는다. 넘기는 차수와 같은 묶음의 뒤 차수다. */
    record Rewrite(List<RoundTimes> next) implements AdvanceDecision {
    }
}
