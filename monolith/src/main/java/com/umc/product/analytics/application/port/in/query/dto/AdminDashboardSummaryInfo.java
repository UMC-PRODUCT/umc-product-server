package com.umc.product.analytics.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.EnumMap;
import java.util.Map;

public record AdminDashboardSummaryInfo(
    long activeChallengerCount,
    long newMemberCountThisWeek,
    double newMemberDeltaPercent,
    long activeSchoolCount,
    long activeChapterCount,
    PointSumInfo monthlyPointSum,
    Map<ChallengerStatus, Long> challengerStatusDistribution
) {

    public static AdminDashboardSummaryInfo of(
        long activeChallengerCount,
        long newMemberCountThisWeek,
        double newMemberDeltaPercent,
        long activeSchoolCount,
        long activeChapterCount,
        PointSumInfo monthlyPointSum,
        Map<ChallengerStatus, Long> challengerStatusDistribution
    ) {
        return new AdminDashboardSummaryInfo(
            activeChallengerCount,
            newMemberCountThisWeek,
            newMemberDeltaPercent,
            activeSchoolCount,
            activeChapterCount,
            monthlyPointSum,
            normalizeStatusDistribution(challengerStatusDistribution)
        );
    }

    public static AdminDashboardSummaryInfo empty() {
        return of(0, 0, 0.0, 0, 0, PointSumInfo.of(0, 0), Map.of());
    }

    private static Map<ChallengerStatus, Long> normalizeStatusDistribution(Map<ChallengerStatus, Long> source) {
        Map<ChallengerStatus, Long> distribution = new EnumMap<>(ChallengerStatus.class);
        for (ChallengerStatus status : ChallengerStatus.values()) {
            distribution.put(status, source.getOrDefault(status, 0L));
        }
        return Map.copyOf(distribution);
    }

    public record PointSumInfo(long positive, long negative) {

        public static PointSumInfo of(long positive, long negative) {
            return new PointSumInfo(positive, negative);
        }
    }
}
