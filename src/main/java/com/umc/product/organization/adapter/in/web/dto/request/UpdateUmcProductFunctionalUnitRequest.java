package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import jakarta.validation.constraints.Size;

public record UpdateUmcProductFunctionalUnitRequest(
    Long parentUnitId,
    UmcProductFunctionalUnitType type,
    @Size(max = 64) String code,
    @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Integer sortOrder,
    Boolean active
) {
    public UpdateUmcProductFunctionalUnitCommand toCommand(Long functionalUnitId, Long requesterMemberId) {
        return UpdateUmcProductFunctionalUnitCommand.of(
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
