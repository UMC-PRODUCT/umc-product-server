package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;

public record UmcProductSquadParticipantCommand(
    Long umcProductMemberId,
    UmcProductSquadRole role,
    UmcProductPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductSquadParticipantCommand of(
        Long umcProductMemberId,
        UmcProductSquadRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new UmcProductSquadParticipantCommand(
            umcProductMemberId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
