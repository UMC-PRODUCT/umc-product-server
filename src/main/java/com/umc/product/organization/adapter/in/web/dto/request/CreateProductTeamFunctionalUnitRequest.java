package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamFunctionalUnitCommand;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductTeamFunctionalUnitRequest(
    @NotNull Long productTeamGenerationId,
    Long parentUnitId,
    @NotNull ProductTeamFunctionalUnitType type,
    @NotNull @Size(max = 64) String code,
    @NotNull @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Integer sortOrder,
    Boolean active
) {
    public CreateProductTeamFunctionalUnitCommand toCommand(Long requesterMemberId) {
        return CreateProductTeamFunctionalUnitCommand.of(
            requesterMemberId,
            productTeamGenerationId,
            parentUnitId,
            type,
            code,
            name,
            description,
            sortOrder == null ? 0 : sortOrder,
            !Boolean.FALSE.equals(active)
        );
    }
}
