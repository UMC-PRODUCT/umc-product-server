package com.umc.product.schedule.application.port.in.query.dto;

import static com.umc.product.schedule.domain.ScheduleConstants.KST;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record MyAttendanceHistoryInfo(
    Long attendanceId,
    Long scheduleId,
    String scheduleName,
    Instant scheduledAt,
    String weekDisplay,       // "1주차"
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
            "",  // TODO: Schedule에 week 필드 추가 필요
            schedule.getStartsAt().atZone(KST).format(DATE_FORMATTER),
            record.getStatus(),
            record.getStatusDisplay()
        );
    }
}
