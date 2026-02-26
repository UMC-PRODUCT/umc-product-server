package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.Gisu;
import java.time.Instant;

public record GisuInfo(
    Long gisuId,
    Long generation,
    Long gisu,// TODO: gisu로 마이그레이션 후 제거할 것
    Instant startAt,
    Instant endAt,
    boolean isActive) {

    public static GisuInfo from(Gisu gisu) {
        return new GisuInfo(
            gisu.getId(),
            gisu.getGeneration(),
            gisu.getGeneration(),
            gisu.getStartAt(),
            gisu.getEndAt(),
            gisu.isActive()
        );
    }
}
