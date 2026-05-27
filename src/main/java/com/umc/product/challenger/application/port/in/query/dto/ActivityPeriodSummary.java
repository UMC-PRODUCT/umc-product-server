package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;
import java.util.Map;

/**
 * 회원의 기수별 활동일 요약 정보입니다.
 * <p>
 * 활동일 산정은 챌린저 상태가 ACTIVE 또는 GRADUATED인 기수에 한하며,
 * 진행 중인 기수는 요청 시점(now)까지의 일수로 계산합니다.
 */
public record ActivityPeriodSummary(
    long totalActivityDays,
    List<PerGisu> perGisu
) {
    /**
     * 단일 기수의 활동일 항목입니다.
     */
    public record PerGisu(
        Long gisuId,
        Long generation,
        long activityDays
    ) {
    }

    public static ActivityPeriodSummary empty() {
        return new ActivityPeriodSummary(0L, List.of());
    }

    public static ActivityPeriodSummary of(Map<Long, PerGisu> perGisuByGisuId) {
        long total = perGisuByGisuId.values().stream()
            .mapToLong(PerGisu::activityDays)
            .sum();
        return new ActivityPeriodSummary(total, perGisuByGisuId.values().stream().toList());
    }
}
