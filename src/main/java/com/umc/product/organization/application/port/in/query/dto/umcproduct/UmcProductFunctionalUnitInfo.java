package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public record UmcProductFunctionalUnitInfo(
    Long functionalUnitId,
    Long umcProductGenerationId,
    Long parentUnitId,
    UmcProductFunctionalUnitType type,
    String typeName,
    String code,
    String name,
    String description,
    int sortOrder,
    boolean active
) {
    public static UmcProductFunctionalUnitInfo from(UmcProductFunctionalUnit unit) {
        return new UmcProductFunctionalUnitInfo(
            unit.getId(),
            unit.getUmcProductGenerationId(),
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
