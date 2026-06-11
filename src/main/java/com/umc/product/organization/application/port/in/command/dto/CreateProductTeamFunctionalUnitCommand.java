package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;

public record CreateProductTeamFunctionalUnitCommand(
    Long requesterMemberId,
    Long productTeamGenerationId,
    Long parentUnitId,
    ProductTeamFunctionalUnitType type,
    String code,
    String name,
    String description,
    int sortOrder,
    boolean active
) {
    public static CreateProductTeamFunctionalUnitCommand of(
        Long requesterMemberId,
        Long productTeamGenerationId,
        Long parentUnitId,
        ProductTeamFunctionalUnitType type,
        String code,
        String name,
        String description,
        int sortOrder,
        boolean active
    ) {
        return new CreateProductTeamFunctionalUnitCommand(
            requesterMemberId,
            productTeamGenerationId,
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
