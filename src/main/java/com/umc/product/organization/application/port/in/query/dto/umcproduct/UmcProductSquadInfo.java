package com.umc.product.organization.application.port.in.query.dto.umcproduct;

import java.time.Instant;

import com.umc.product.organization.domain.UmcProductSquad;

public record UmcProductSquadInfo(
    Long squadId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static UmcProductSquadInfo from(UmcProductSquad squad) {
        return new UmcProductSquadInfo(
            squad.getId(),
            squad.getCode(),
            squad.getName(),
            squad.getDescription(),
            squad.getStartAt(),
            squad.getEndAt(),
            squad.getSortOrder(),
            squad.isActive()
        );
    }
}
