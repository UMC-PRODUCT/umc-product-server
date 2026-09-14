package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.Map;

public record AdminDashboardSummaryResponse(
    long activeChallengerCount,
    long newMemberCountThisWeek,
    double newMemberDeltaPercent,
    long activeSchoolCount,
    long activeChapterCount,
    PointSumResponse monthlyPointSum,
    Map<ChallengerStatus, Long> challengerStatusDistribution
) {

    public static AdminDashboardSummaryResponse from(AdminDashboardSummaryInfo info) {
        return new AdminDashboardSummaryResponse(
            info.activeChallengerCount(),
            info.newMemberCountThisWeek(),
            info.newMemberDeltaPercent(),
            info.activeSchoolCount(),
            info.activeChapterCount(),
            PointSumResponse.from(info.monthlyPointSum()),
            info.challengerStatusDistribution()
        );
    }

    public record PointSumResponse(long positive, long negative) {

        public static PointSumResponse from(AdminDashboardSummaryInfo.PointSumInfo info) {
            return new PointSumResponse(info.positive(), info.negative());
        }
    }
}
