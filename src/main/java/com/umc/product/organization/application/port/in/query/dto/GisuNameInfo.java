package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.Gisu;

public record GisuNameInfo(
        Long gisuId,
        Long generation,
        boolean isActive
) {
    public static GisuNameInfo from(Gisu gisu) {
        return new GisuNameInfo(gisu.getId(), gisu.getGeneration(), gisu.isActive());
    }
}
