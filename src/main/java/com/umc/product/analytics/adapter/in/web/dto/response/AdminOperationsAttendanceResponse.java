package com.umc.product.analytics.adapter.in.web.dto.response;

import java.util.Map;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsAttendanceInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;

import lombok.Builder;

@Builder
public record AdminOperationsAttendanceResponse(
    long scheduleCount,
    long attendanceRequiredScheduleCount,
    long attendanceRecordCount,
    Map<AttendanceStatus, Long> attendanceStatusCounts
) {

    public static AdminOperationsAttendanceResponse from(AdminOperationsAttendanceInfo info) {
        return AdminOperationsAttendanceResponse.builder()
            .scheduleCount(info.scheduleCount())
            .attendanceRequiredScheduleCount(info.attendanceRequiredScheduleCount())
            .attendanceRecordCount(info.attendanceRecordCount())
            .attendanceStatusCounts(info.attendanceStatusCounts())
            .build();
    }
}
