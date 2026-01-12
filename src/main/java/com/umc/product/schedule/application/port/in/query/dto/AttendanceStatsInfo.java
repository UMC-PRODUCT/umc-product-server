package com.umc.product.schedule.application.port.in.query.dto;

public record AttendanceStatsInfo(
        Long scheduleId,
        Integer totalCount,
        Integer presentCount,
        Integer pendingCount,
        Double attendanceRate
) {
    public static AttendanceStatsInfo of(Long scheduleId, Integer totalCount, Integer presentCount,
                                         Integer pendingCount) {
        double rate = totalCount > 0 ? (presentCount * 100.0) / totalCount : 0.0;
        return new AttendanceStatsInfo(
                scheduleId,
                totalCount,
                presentCount,
                pendingCount,
                Math.round(rate * 10) / 10.0  // 소수점 1자리
        );
    }
}
