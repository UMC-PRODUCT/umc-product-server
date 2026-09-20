package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;

public record AdminDashboardActionQueueResponse(
    long pendingAttendanceDecisionCount,
    long newRiskMemberCountThisWeek,
    long upcomingGraduationCount
) {

    public static AdminDashboardActionQueueResponse from(AdminDashboardActionQueueInfo info) {
        return new AdminDashboardActionQueueResponse(
            info.pendingAttendanceDecisionCount(),
            info.newRiskMemberCountThisWeek(),
            info.upcomingGraduationCount()
        );
    }
}
