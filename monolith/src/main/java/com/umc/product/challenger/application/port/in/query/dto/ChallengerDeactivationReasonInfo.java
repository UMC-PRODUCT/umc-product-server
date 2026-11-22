package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;

public record ChallengerDeactivationReasonInfo(
    ChallengerDeactivationReason reason,
    String displayName,
    String description,
    boolean reasonDetailRequired
) {
    public static ChallengerDeactivationReasonInfo from(ChallengerDeactivationReason reason) {
        return new ChallengerDeactivationReasonInfo(
            reason,
            reason.getDisplayName(),
            reason.getDescription(),
            reason.isOther()
        );
    }
}
