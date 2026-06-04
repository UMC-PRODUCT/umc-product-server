package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ProductTeamActivityCommand;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import jakarta.validation.constraints.NotNull;

public record ProductTeamActivityRequest(
    @NotNull Long productTeamGenerationId,
    @NotNull ProductTeamPart part,
    @NotNull ProductTeamRole role,
    @NotNull ProductTeamPosition position
) {
    public ProductTeamActivityCommand toCommand() {
        return ProductTeamActivityCommand.of(productTeamGenerationId, part, role, position);
    }
}
