package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;

public record ProductTeamSquadParticipantCommand(
    Long productTeamMemberId,
    ProductTeamSquadRole role,
    ProductTeamPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamSquadParticipantCommand of(
        Long productTeamMemberId,
        ProductTeamSquadRole role,
        ProductTeamPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new ProductTeamSquadParticipantCommand(
            productTeamMemberId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
