package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalTime;

// TODO : 주석 처리 부분 tags 로 변경
public record AvailableAttendanceInfo(
        // ==== Schedule 정보 ====
        Long scheduleId,
        String scheduleName,
//        ScheduleType scheduleType,
        LocalTime startTime,
        LocalTime endTime,

        // ==== AttendanceSheet 정보 ====
        Long sheetId,

        // ==== AttendanceRecord 정보 ====
        Long recordId,              // null이면 아직 출석 안함
        AttendanceStatus status,
        String statusDisplay
) {
    /**
     * 출석 기록이 없는 경우 (아직 출석 안함) - 출석부는 있지만 해당 챌린저가 아직 출석 체크 안함
     */
    public static AvailableAttendanceInfo of(Schedule schedule, AttendanceSheet sheet) {
        return new AvailableAttendanceInfo(
                schedule.getId(),
                schedule.getName(),
//                schedule.getType(),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                sheet.getId(),
                null,
                AttendanceStatus.PENDING,
                "출석 전"
        );
    }

    /**
     * 출석 기록이 있는 경우 - 이미 출석 체크를 완료했거나 승인 대기 중
     */
    public static AvailableAttendanceInfo of(Schedule schedule, AttendanceSheet sheet, AttendanceRecord record) {
        return new AvailableAttendanceInfo(
                schedule.getId(),
                schedule.getName(),
//                schedule.getType(),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                sheet.getId(),
                record.getId(),
                record.getStatus(),
                resolveStatusDisplay(record.getStatus())
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
