package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;

public record ChallengerDeactivationReasonListInfo(
    List<ChallengerDeactivationReasonInfo> voluntaryReasons,
    List<ChallengerDeactivationReasonInfo> involuntaryReasons
) {
    public ChallengerDeactivationReasonListInfo {
        voluntaryReasons = List.copyOf(voluntaryReasons);
        involuntaryReasons = List.copyOf(involuntaryReasons);
    }
}
