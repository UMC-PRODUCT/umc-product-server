package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.vo.AttendanceStats;

public record AttendanceStatsInfo(
    Long scheduleId,
    Integer totalCount,
    Integer presentCount,
    Integer pendingCount,
    Double attendanceRate
) {
    public static AttendanceStatsInfo of(Long scheduleId, AttendanceStats stats) {
        return new AttendanceStatsInfo(
            scheduleId,
            stats.totalCount(),
            stats.presentCount(),
            stats.pendingCount(),
            stats.calculateAttendanceRate() // VO가 계산해준 결과값을 가져오기만 함
        );
    }
}
