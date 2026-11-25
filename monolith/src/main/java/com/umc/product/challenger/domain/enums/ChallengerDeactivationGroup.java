package com.umc.product.challenger.domain.enums;

import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerDeactivationGroup {
    VOLUNTARY(ChallengerStatus.WITHDRAWN),
    INVOLUNTARY(ChallengerStatus.EXPELLED);

    private final ChallengerStatus status;
}
