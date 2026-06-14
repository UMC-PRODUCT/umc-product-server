package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUmcProductFunctionalUnitRequest(
    @NotNull Long umcProductGenerationId,
    Long parentUnitId,
    @NotNull UmcProductFunctionalUnitType type,
    @NotNull @Size(max = 64) String code,
    @NotNull @Size(max = 100) String name,
    @Size(max = 1000) String description,
    Integer sortOrder,
    Boolean active
) {
    public CreateUmcProductFunctionalUnitCommand toCommand(Long requesterMemberId) {
        return CreateUmcProductFunctionalUnitCommand.of(
            requesterMemberId,
            umcProductGenerationId,
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
