package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;

public record ProductTeamFunctionalUnitResponse(
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
    public static ProductTeamFunctionalUnitResponse from(ProductTeamFunctionalUnitInfo info) {
        return new ProductTeamFunctionalUnitResponse(
            info.functionalUnitId(),
            info.productTeamGenerationId(),
            info.parentUnitId(),
            info.type(),
            info.typeName(),
            info.code(),
            info.name(),
            info.description(),
            info.sortOrder(),
            info.active()
        );
    }
}
