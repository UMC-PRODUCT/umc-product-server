package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public record UmcProductFunctionalUnitResponse(
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
    public static UmcProductFunctionalUnitResponse from(UmcProductFunctionalUnitInfo info) {
        return new UmcProductFunctionalUnitResponse(
            info.functionalUnitId(),
            info.umcProductGenerationId(),
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
