package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;

public record UpdateProductTeamFunctionalUnitCommand(
    Long functionalUnitId,
    Long requesterMemberId,
    Long parentUnitId,
    ProductTeamFunctionalUnitType type,
    String code,
    String name,
    String description,
    Integer sortOrder,
    Boolean active
) {
    public static UpdateProductTeamFunctionalUnitCommand of(
        Long functionalUnitId,
        Long requesterMemberId,
        Long parentUnitId,
        ProductTeamFunctionalUnitType type,
        String code,
        String name,
        String description,
        Integer sortOrder,
        Boolean active
    ) {
        return new UpdateProductTeamFunctionalUnitCommand(
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
