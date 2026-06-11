package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;

public record ProductTeamSquadParticipationCommand(
    Long squadId,
    ProductTeamSquadRole role,
    ProductTeamPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamSquadParticipationCommand of(
        Long squadId,
        ProductTeamSquadRole role,
        ProductTeamPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new ProductTeamSquadParticipationCommand(
            squadId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
