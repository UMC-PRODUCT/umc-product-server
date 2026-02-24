package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleConstants;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record MyAttendanceHistoryInfo(
    Long attendanceId,
    Long scheduleId,
    String scheduleName,
    Instant scheduledAt,
    List<ScheduleTag> tags,
    LocalDate scheduledDate,     // "2024-01-15"
    Instant startTime,         // "14:30"
    Instant endTime,           // "16:00"
    AttendanceStatus status,
    String statusDisplay,     // "출석", "지각", "결석"

    // 추가 필드
    Long sheetId,             // 출석부 ID
    String locationName,      // 일정 장소명
    Boolean locationVerified, // 위치 인증 여부
    String memo,              // 출석 메모 (사유 등)
    Instant checkedAt   // 실제 출석 체크한 시간
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * AttendanceSheet 없이 생성 (sheet 정보가 불필요한 경우)
     */
    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceRecord record) {
        return of(schedule, null, record);
    }

    /**
     * 전체 정보로 생성 (sheet는 nullable)
     */
    public static MyAttendanceHistoryInfo of(Schedule schedule, AttendanceSheet sheet, AttendanceRecord record) {
        return new MyAttendanceHistoryInfo(
            record.getId(),
            schedule.getId(),
            schedule.getName(),
            schedule.getStartsAt(),
            schedule.getTags().stream().toList(),
            schedule.getStartsAt().atZone(ScheduleConstants.KST).toLocalDate(),
            schedule.getStartsAt(),
            schedule.getEndsAt(),
            record.getStatus(),
            record.getStatusDisplay(),
            sheet != null ? sheet.getId() : null,  // null-safe
            schedule.getLocationName(),
            record.isChecked() ? record.isLocationVerified() : null,
            record.getMemo(),
            record.getCheckedAt()
        );
    }
}
