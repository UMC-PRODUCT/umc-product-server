package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
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
    String scheduledDate,     // "2024-01-15"
    String startTime,         // "14:30"
    String endTime,           // "16:00"
    AttendanceStatus status,
    String statusDisplay,     // "출석", "지각", "결석"

    // 추가 필드
    Long sheetId,             // 출석부 ID
    String locationName,      // 일정 장소명
    Boolean locationVerified, // 위치 인증 여부
    String memo,              // 출석 메모 (사유 등)
    LocalDateTime checkedAt   // 실제 출석 체크한 시간
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceRecord record) {
        return new MyAttendanceHistoryInfo(
            record.getId(),
            schedule.getId(),
            schedule.getName(),
            schedule.getStartsAt(),
            schedule.getTags().stream().toList(),
            schedule.getStartsAt().format(DATE_FORMATTER),
            schedule.getStartsAt().format(TIME_FORMATTER),
            schedule.getEndsAt().format(TIME_FORMATTER),
            record.getStatus(),
            record.getStatusDisplay(),
            null,  // sheetId는 AttendanceSheet가 필요
            schedule.getLocationName(),
            record.isChecked() ? record.isLocationVerified() : null,
            record.getMemo(),
            record.getCheckedAt()
        );
    }

    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceSheet sheet, AttendanceRecord record) {
        return new MyAttendanceHistoryInfo(
            record.getId(),
            schedule.getId(),
            schedule.getName(),
            schedule.getStartsAt(),
            schedule.getTags().stream().toList(),
            schedule.getStartsAt().format(DATE_FORMATTER),
            schedule.getStartsAt().format(TIME_FORMATTER),
            schedule.getEndsAt().format(TIME_FORMATTER),
            record.getStatus(),
            record.getStatusDisplay(),
            sheet.getId(),
            schedule.getLocationName(),
            record.isChecked() ? record.isLocationVerified() : null,
            record.getMemo(),
            record.getCheckedAt()
        );
    }
}
