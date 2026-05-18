package com.umc.product.organization.application.port.in.query.dto.gisu;

import com.umc.product.organization.domain.Gisu;
import java.time.Duration;
import java.time.Instant;

public record GisuInfo(
    Long gisuId,
    Long generation,
    Instant startAt,
    Instant endAt,
    boolean isActive) {

    public static GisuInfo from(Gisu gisu) {
        return new GisuInfo(
            gisu.getId(),
            gisu.getGeneration(),
            gisu.getStartAt(),
            gisu.getEndAt(),
            gisu.isActive()
        );
    }

    /**
     * 주어진 시점(now) 기준의 활동일을 반환합니다. {@link Gisu#activityDays(Instant)}와 동일한 규칙을 따릅니다.
     */
    public long activityDays(Instant now) {
        if (now.isBefore(startAt)) {
            return 0L;
        }
        Instant effectiveEnd = now.isBefore(endAt) ? now : endAt;
        return Duration.between(startAt, effectiveEnd).toDays();
    }
}
