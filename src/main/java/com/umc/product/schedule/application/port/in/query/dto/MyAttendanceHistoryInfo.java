package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record MyAttendanceHistoryInfo(
    Long attendanceId,
    Long scheduleId,
    String scheduleName,
    LocalDateTime scheduledAt,
    List<ScheduleTag> tags,
    String time,              // "14:30"
    AttendanceStatus status,
    String statusDisplay      // "출석", "지각", "결석"
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceRecord record) {
        return new MyAttendanceHistoryInfo(
            record.getId(),
            schedule.getId(),
            schedule.getName(),
            schedule.getStartsAt(),
            schedule.getTags().stream().toList(),
            schedule.getStartsAt().format(TIME_FORMATTER),
            record.getStatus(),
            record.getStatusDisplay()
        );
    }
}
