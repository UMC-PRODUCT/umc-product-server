package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalMembershipInfo;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamPosition;

public record ProductTeamFunctionalMembershipResponse(
    Long functionalMembershipId,
    Long productTeamGenerationId,
    Long generation,
    Long functionalUnitId,
    ProductTeamFunctionalUnitResponse functionalUnit,
    ProductTeamFunctionalRole role,
    String roleName,
    ProductTeamPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamFunctionalMembershipResponse from(ProductTeamFunctionalMembershipInfo info) {
        return new ProductTeamFunctionalMembershipResponse(
            info.functionalMembershipId(),
            info.productTeamGenerationId(),
            info.generation(),
            info.functionalUnitId(),
            info.functionalUnit() == null ? null : ProductTeamFunctionalUnitResponse.from(info.functionalUnit()),
            info.role(),
            info.roleName(),
            info.position(),
            info.positionName(),
            info.responsibilityTitle(),
            info.responsibilityDescription()
        );
    }
}
