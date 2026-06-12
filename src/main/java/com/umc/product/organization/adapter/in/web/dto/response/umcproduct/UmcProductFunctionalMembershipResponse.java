package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalMembershipInfo;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductPosition;

public record UmcProductFunctionalMembershipResponse(
    Long functionalMembershipId,
    Long umcProductGenerationId,
    Long generation,
    Long functionalUnitId,
    UmcProductFunctionalUnitResponse functionalUnit,
    UmcProductFunctionalRole role,
    String roleName,
    UmcProductPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductFunctionalMembershipResponse from(UmcProductFunctionalMembershipInfo info) {
        return new UmcProductFunctionalMembershipResponse(
            info.functionalMembershipId(),
            info.umcProductGenerationId(),
            info.generation(),
            info.functionalUnitId(),
            info.functionalUnit() == null ? null : UmcProductFunctionalUnitResponse.from(info.functionalUnit()),
            info.role(),
            info.roleName(),
            info.position(),
            info.positionName(),
            info.responsibilityTitle(),
            info.responsibilityDescription()
        );
    }
}
