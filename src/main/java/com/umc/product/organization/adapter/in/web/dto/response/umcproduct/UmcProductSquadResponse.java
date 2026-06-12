package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import java.time.Instant;

public record UmcProductSquadResponse(
    Long squadId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static UmcProductSquadResponse from(UmcProductSquadInfo info) {
        return new UmcProductSquadResponse(
            info.squadId(),
            info.code(),
            info.name(),
            info.description(),
            info.startAt(),
            info.endAt(),
            info.sortOrder(),
            info.active()
        );
    }
}
