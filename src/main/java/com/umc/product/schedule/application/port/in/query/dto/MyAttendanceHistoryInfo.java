package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MyAttendanceHistoryInfo(
        Long attendanceId,
        Long scheduleId,
        String scheduleName,
        LocalDateTime scheduledAt,
        String dateDisplay,       // "03.16"
        AttendanceStatus status,
        String statusDisplay      // "출석", "지각", "결석"
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");

    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceRecord record) {
        return new MyAttendanceHistoryInfo(
                record.getId(),
                schedule.getId(),
                schedule.getName(),
                schedule.getStartsAt(),
                schedule.getStartsAt().format(DATE_FORMATTER),
                record.getStatus(),
                record.getStatusDisplay()
        );
    }
}
