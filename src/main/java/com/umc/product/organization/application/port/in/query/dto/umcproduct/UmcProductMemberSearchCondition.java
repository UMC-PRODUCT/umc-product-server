package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;

public record UmcProductMemberSearchCondition(
    Long umcProductGenerationId,
    Long functionalUnitId,
    UmcProductFunctionalUnitType functionalUnitType,
    UmcProductFunctionalRole role,
    UmcProductPosition position,
    Long squadId
) {
    public static UmcProductMemberSearchCondition of(
        Long umcProductGenerationId,
        Long functionalUnitId,
        UmcProductFunctionalUnitType functionalUnitType,
        UmcProductFunctionalRole role,
        UmcProductPosition position,
        Long squadId
    ) {
        return new UmcProductMemberSearchCondition(
            umcProductGenerationId,
            functionalUnitId,
            functionalUnitType,
            role,
            position,
            squadId
        );
    }
}
