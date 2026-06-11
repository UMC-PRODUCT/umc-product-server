package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamFunctionalUnitCommand;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import jakarta.validation.constraints.Size;

public record UpdateProductTeamFunctionalUnitRequest(
    Long parentUnitId,
    ProductTeamFunctionalUnitType type,
    @Size(max = 64) String code,
    @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Integer sortOrder,
    Boolean active
) {
    public UpdateProductTeamFunctionalUnitCommand toCommand(Long functionalUnitId, Long requesterMemberId) {
        return UpdateProductTeamFunctionalUnitCommand.of(
            functionalUnitId,
            requesterMemberId,
            parentUnitId,
            type,
            code,
            name,
            description,
            sortOrder,
            active
        );
    }
}
