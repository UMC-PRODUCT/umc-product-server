package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;

public record ProductTeamFunctionalUnitInfo(
    Long functionalUnitId,
    Long productTeamGenerationId,
    Long parentUnitId,
    ProductTeamFunctionalUnitType type,
    String typeName,
    String code,
    String name,
    String description,
    int sortOrder,
    boolean active
) {
    public static ProductTeamFunctionalUnitInfo from(ProductTeamFunctionalUnit unit) {
        return new ProductTeamFunctionalUnitInfo(
            unit.getId(),
            unit.getProductTeamGenerationId(),
            unit.getParentUnitId(),
            unit.getType(),
            unit.getType().getDisplayName(),
            unit.getCode(),
            unit.getName(),
            unit.getDescription(),
            unit.getSortOrder(),
            unit.isActive()
        );
    }
}
