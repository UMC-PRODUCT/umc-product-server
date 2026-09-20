package com.umc.product.analytics.application.port.in.query.dto;

public record AdminDashboardActionQueueInfo(
    long pendingAttendanceDecisionCount,
    long newRiskMemberCountThisWeek,
    long upcomingGraduationCount
) {

    public static AdminDashboardActionQueueInfo of(
        long pendingAttendanceDecisionCount,
        long newRiskMemberCountThisWeek,
        long upcomingGraduationCount
    ) {
        return new AdminDashboardActionQueueInfo(
            pendingAttendanceDecisionCount,
            newRiskMemberCountThisWeek,
            upcomingGraduationCount
        );
    }
}
