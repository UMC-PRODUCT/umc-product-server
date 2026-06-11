package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamPosition;

public record ProductTeamFunctionalMembershipCommand(
    Long productTeamGenerationId,
    Long functionalUnitId,
    ProductTeamFunctionalRole role,
    ProductTeamPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static ProductTeamFunctionalMembershipCommand of(
        Long productTeamGenerationId,
        Long functionalUnitId,
        ProductTeamFunctionalRole role,
        ProductTeamPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new ProductTeamFunctionalMembershipCommand(
            productTeamGenerationId,
            functionalUnitId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
