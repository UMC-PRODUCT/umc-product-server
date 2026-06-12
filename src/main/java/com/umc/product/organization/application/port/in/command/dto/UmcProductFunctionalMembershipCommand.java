package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductPosition;

public record UmcProductFunctionalMembershipCommand(
    Long umcProductGenerationId,
    Long functionalUnitId,
    UmcProductFunctionalRole role,
    UmcProductPosition position,
    String responsibilityTitle,
    String responsibilityDescription
) {
    public static UmcProductFunctionalMembershipCommand of(
        Long umcProductGenerationId,
        Long functionalUnitId,
        UmcProductFunctionalRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return new UmcProductFunctionalMembershipCommand(
            umcProductGenerationId,
            functionalUnitId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
