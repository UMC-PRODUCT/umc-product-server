package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.Gisu;

public record GisuNameInfo(
    Long gisuId,
    Long generation,
    // TODO: gisu로 마이그레이션 후 제거할 것
    Long gisu,
    boolean isActive
) {
    public static GisuNameInfo from(Gisu gisu) {
        return new GisuNameInfo(
            gisu.getId(),
            gisu.getGeneration(),
            gisu.getGeneration(),
            gisu.isActive());
    }
}
