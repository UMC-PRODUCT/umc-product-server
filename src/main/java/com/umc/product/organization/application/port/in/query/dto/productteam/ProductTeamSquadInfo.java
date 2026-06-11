package com.umc.product.organization.application.port.in.query.dto.productteam;

import com.umc.product.organization.domain.ProductTeamSquad;
import java.time.Instant;

public record ProductTeamSquadInfo(
    Long squadId,
    String code,
    String name,
    String description,
    Instant startAt,
    Instant endAt,
    int sortOrder,
    boolean active
) {
    public static ProductTeamSquadInfo from(ProductTeamSquad squad) {
        return new ProductTeamSquadInfo(
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
