package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;

public record ProductTeamSquadParticipationInfo(
    Long squadParticipantId,
    Long squadId,
    ProductTeamSquadInfo squad,
    ProductTeamSquadRole role,
    String roleName,
    ProductTeamPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamSquadParticipationInfo from(
        ProductTeamSquadParticipant participant,
        ProductTeamSquadInfo squad
    ) {
        return new ProductTeamSquadParticipationInfo(
            participant.getId(),
            participant.getSquad().getId(),
            squad,
            participant.getRole(),
            participant.getRole().getDisplayName(),
            participant.getPosition(),
            participant.getPosition().getDisplayName(),
            participant.getResponsibilityTitle(),
            participant.getResponsibilityDescription()
        );
    }
}
