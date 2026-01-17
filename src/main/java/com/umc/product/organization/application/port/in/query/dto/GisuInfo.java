package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.Gisu;
import java.time.LocalDate;

public record GisuInfo(Long gisuId, Long generation, LocalDate startAt, LocalDate endAt, boolean isActive) {

    public static GisuInfo from(Gisu gisu) {
        return new GisuInfo(
                gisu.getId(),
                gisu.getGeneration(),
                gisu.getStartAt().toLocalDate(),
                gisu.getEndAt().toLocalDate(),
                gisu.isActive()
        );
    }
}
