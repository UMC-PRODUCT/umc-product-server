package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleAttendance;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleType;
import java.time.LocalTime;

public record AvailableAttendanceInfo(
        // ==== Schedule 정보 ====
        Long scheduleId,
        String scheduleName,
        ScheduleType scheduleType,
        LocalTime startTime,
        LocalTime endTime,
        String locationName,
        Double locationLatitude,
        Double locationLongitude,
        Integer attendanceRadius,

        // ==== Attendance 정보 ====
        Long attendanceId,
        AttendanceStatus status,  // PENDING, PRESENT_PENDING, LATE_PENDING 등
        String statusDisplay      // "출석 전", "승인 대기", "출석 완료"
) {
    public static AvailableAttendanceInfo of(Schedule schedule, ScheduleAttendance attendance) {
        return new AvailableAttendanceInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getType(),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                schedule.getLocationName(),
                schedule.getLatitude(),
                schedule.getLongitude(),
                schedule.getAttendanceRadius(),
                attendance.getId(),
                attendance.getStatus(),
                resolveStatusDisplay(attendance.getStatus())
        );
    }

    private static String resolveStatusDisplay(AttendanceStatus status) {
        return switch (status) {
            case PENDING -> "출석 전";
            case PRESENT_PENDING, LATE_PENDING, EXCUSED_PENDING -> "승인 대기";
            case PRESENT, LATE, EXCUSED -> "출석 완료";
            case ABSENT -> "결석";
        };
    }
}
