package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UmcProductFunctionalMembershipCommand;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductPosition;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UmcProductFunctionalMembershipRequest(
    @NotNull Long umcProductGenerationId,
    @NotNull Long functionalUnitId,
    @NotNull UmcProductFunctionalRole role,
    @NotNull UmcProductPosition position,
    @Size(max = 200) String responsibilityTitle,
    @Size(max = 1000) String responsibilityDescription
) {
    public UmcProductFunctionalMembershipCommand toCommand() {
        return UmcProductFunctionalMembershipCommand.of(
            umcProductGenerationId,
            functionalUnitId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
