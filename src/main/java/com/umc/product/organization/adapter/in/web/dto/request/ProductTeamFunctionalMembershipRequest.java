package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ProductTeamFunctionalMembershipCommand;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductTeamFunctionalMembershipRequest(
    @NotNull Long productTeamGenerationId,
    @NotNull Long functionalUnitId,
    @NotNull ProductTeamFunctionalRole role,
    @NotNull ProductTeamPosition position,
    @Size(max = 200) String responsibilityTitle,
    @Size(max = 1000) String responsibilityDescription
) {
    public ProductTeamFunctionalMembershipCommand toCommand() {
        return ProductTeamFunctionalMembershipCommand.of(
            productTeamGenerationId,
            functionalUnitId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
