package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadParticipationInfo;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;

public record ProductTeamSquadParticipationResponse(
    Long squadParticipantId,
    Long squadId,
    ProductTeamSquadResponse squad,
    ProductTeamSquadRole role,
    String roleName,
    ProductTeamPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamSquadParticipationResponse from(ProductTeamSquadParticipationInfo info) {
        return new ProductTeamSquadParticipationResponse(
            info.squadParticipantId(),
            info.squadId(),
            info.squad() == null ? null : ProductTeamSquadResponse.from(info.squad()),
            info.role(),
            info.roleName(),
            info.position(),
            info.positionName(),
            info.responsibilityTitle(),
            info.responsibilityDescription()
        );
    }
}
