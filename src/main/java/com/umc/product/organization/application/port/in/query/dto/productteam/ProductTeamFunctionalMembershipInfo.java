package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamPosition;

public record ProductTeamFunctionalMembershipInfo(
    Long functionalMembershipId,
    Long productTeamGenerationId,
    Long generation,
    Long functionalUnitId,
    ProductTeamFunctionalUnitInfo functionalUnit,
    ProductTeamFunctionalRole role,
    String roleName,
    ProductTeamPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamFunctionalMembershipInfo from(
        ProductTeamFunctionalMembership membership,
        ProductTeamGenerationInfo generation,
        ProductTeamFunctionalUnitInfo functionalUnit
    ) {
        return new ProductTeamFunctionalMembershipInfo(
            membership.getId(),
            membership.getProductTeamGenerationId(),
            generation == null ? null : generation.generation(),
            membership.getFunctionalUnitId(),
            functionalUnit,
            membership.getRole(),
            membership.getRole().getDisplayName(),
            membership.getPosition(),
            membership.getPosition().getDisplayName(),
            membership.getResponsibilityTitle(),
            membership.getResponsibilityDescription()
        );
    }
}
