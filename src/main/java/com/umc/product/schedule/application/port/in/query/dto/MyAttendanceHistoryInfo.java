package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.format.DateTimeFormatter;

public record MyAttendanceHistoryInfo(
        Long attendanceId,
        Long scheduleId,
        String scheduleName,
        String weekDisplay,       // 3주차, 2주차
        String dateDisplay,       // 03.16
        AttendanceStatus status,
        String statusDisplay      // "출석", "지각", "결석"
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");

    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceRecord record, Integer weekNo) {
        return new MyAttendanceHistoryInfo(
                record.getId(),
                schedule.getId(),
                schedule.getName(),
                weekNo + "주차",
                schedule.getStartsAt().format(DATE_FORMATTER),
                record.getStatus(),
                resolveStatusDisplay(record.getStatus())
        );
    }

    private static String resolveStatusDisplay(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
            case EXCUSED -> "인정";
            case PENDING, PRESENT_PENDING, LATE_PENDING, EXCUSED_PENDING -> "대기";
        };
    }
}
