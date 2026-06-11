package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import com.umc.product.organization.domain.enums.ProductTeamPosition;

public record ProductTeamMemberSearchCondition(
    Long productTeamGenerationId,
    Long functionalUnitId,
    ProductTeamFunctionalUnitType functionalUnitType,
    ProductTeamFunctionalRole role,
    ProductTeamPosition position,
    Long squadId
) {
    public static ProductTeamMemberSearchCondition of(
        Long productTeamGenerationId,
        Long functionalUnitId,
        ProductTeamFunctionalUnitType functionalUnitType,
        ProductTeamFunctionalRole role,
        ProductTeamPosition position,
        Long squadId
    ) {
        return new ProductTeamMemberSearchCondition(
            productTeamGenerationId,
            functionalUnitId,
            functionalUnitType,
            role,
            position,
            squadId
        );
    }
}
