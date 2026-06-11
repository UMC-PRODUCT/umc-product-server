package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import java.time.Instant;

public record ProductTeamSquadResponse(
    Long squadId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static ProductTeamSquadResponse from(ProductTeamSquadInfo info) {
        return new ProductTeamSquadResponse(
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
