package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadParticipationInfo;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;

public record UmcProductSquadParticipationResponse(
    Long squadParticipantId,
    Long squadId,
    UmcProductSquadResponse squad,
    UmcProductSquadRole role,
    String roleName,
    UmcProductPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductSquadParticipationResponse from(UmcProductSquadParticipationInfo info) {
        return new UmcProductSquadParticipationResponse(
            info.squadParticipantId(),
            info.squadId(),
            info.squad() == null ? null : UmcProductSquadResponse.from(info.squad()),
            info.role(),
            info.roleName(),
            info.position(),
            info.positionName(),
            info.responsibilityTitle(),
            info.responsibilityDescription()
        );
    }
}
