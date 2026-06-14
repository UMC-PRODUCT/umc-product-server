package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public record CreateUmcProductFunctionalUnitCommand(
    Long requesterMemberId,
    Long umcProductGenerationId,
    Long parentUnitId,
    UmcProductFunctionalUnitType type,
    String code,
    String name,
    String description,
    int sortOrder,
    boolean active
) {
    public static CreateUmcProductFunctionalUnitCommand of(
        Long requesterMemberId,
        Long umcProductGenerationId,
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name,
        String description,
        int sortOrder,
        boolean active
    ) {
        return new CreateUmcProductFunctionalUnitCommand(
            requesterMemberId,
            umcProductGenerationId,
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
