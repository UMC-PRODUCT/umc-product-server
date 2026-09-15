package com.umc.product.analytics.application.port.in.query.dto;

import java.util.Map;

import com.umc.product.schedule.domain.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record AdminOperationsAttendanceInfo(
    long scheduleCount,
    long attendanceRequiredScheduleCount,
    long attendanceRecordCount,
    Map<AttendanceStatus, Long> attendanceStatusCounts
) {

    public static AdminOperationsAttendanceInfo of(
        long scheduleCount,
        long attendanceRequiredScheduleCount,
        long attendanceRecordCount,
        Map<AttendanceStatus, Long> attendanceStatusCounts
    ) {
        return AdminOperationsAttendanceInfo.builder()
            .scheduleCount(scheduleCount)
            .attendanceRequiredScheduleCount(attendanceRequiredScheduleCount)
            .attendanceRecordCount(attendanceRecordCount)
            .attendanceStatusCounts(Map.copyOf(attendanceStatusCounts))
            .build();
    }
}
