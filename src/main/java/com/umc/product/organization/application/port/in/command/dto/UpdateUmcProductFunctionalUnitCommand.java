package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public record UpdateUmcProductFunctionalUnitCommand(
    Long functionalUnitId,
    Long requesterMemberId,
    Long parentUnitId,
    UmcProductFunctionalUnitType type,
    String code,
    String name,
    String description,
    Integer sortOrder,
    Boolean active
) {
    public static UpdateUmcProductFunctionalUnitCommand of(
        Long functionalUnitId,
        Long requesterMemberId,
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name,
        String description,
        Integer sortOrder,
        Boolean active
    ) {
        return new UpdateUmcProductFunctionalUnitCommand(
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
