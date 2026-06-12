package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;

public record UmcProductSquadParticipationCommand(
    Long squadId,
    UmcProductSquadRole role,
    UmcProductPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductSquadParticipationCommand of(
        Long squadId,
        UmcProductSquadRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new UmcProductSquadParticipationCommand(
            squadId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
