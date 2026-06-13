package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductPosition;

public record UmcProductFunctionalMembershipInfo(
    Long functionalMembershipId,
    Long umcProductGenerationId,
    Long generation,
    Long functionalUnitId,
    UmcProductFunctionalUnitInfo functionalUnit,
    UmcProductFunctionalRole role,
    String roleName,
    UmcProductPosition position,
    String positionName,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductFunctionalMembershipInfo from(
        UmcProductFunctionalMembership membership,
        UmcProductGenerationInfo generation,
        UmcProductFunctionalUnitInfo functionalUnit
    ) {
        return new UmcProductFunctionalMembershipInfo(
            membership.getId(),
            membership.getUmcProductGenerationId(),
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
