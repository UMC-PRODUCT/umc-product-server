package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record ScheduleWithStatsInfo(
        // === Schedule Info ===
        Long scheduleId,
        String name,
        ScheduleType type,
        String status,          // "진행 중", "종료됨", "예정"
        String date,            // yyyy.mm.dd (요일) 형식
        LocalTime startTime,
        LocalTime endTime,
        String locationName,

        // === Attendance Stats ===
        Integer totalCount,
        Integer presentCount,
        Integer pendingCount,
        Double attendanceRate   // 예: 85.0
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)",
            Locale.KOREAN);

    /**
     * Schedule 엔티티와 통계 수치를 받아 응답 DTO 생성
     */
    public static ScheduleWithStatsInfo of(Schedule schedule, Integer totalCount, Integer presentCount,
                                           Integer pendingCount) {
        // 출석률 계산 (0으로 나누기 방지)
        double rate = (totalCount != null && totalCount > 0)
                ? (presentCount * 100.0) / totalCount
                : 0.0;

        return new ScheduleWithStatsInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getType(),
                resolveStatus(schedule),
                schedule.getStartsAt().format(DATE_FORMATTER),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                schedule.getLocationName(),
                totalCount,
                presentCount,
                pendingCount,
                Math.round(rate * 10) / 10.0 // 소수점 첫째 자리 반올림
        );
    }

    // 통계 객체가 이미 있다면 이를 활용하는 오버로딩 메서드
    public static ScheduleWithStatsInfo of(Schedule schedule, AttendanceStatsSummary stats) {
        return new ScheduleWithStatsInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getType(),
                resolveStatus(schedule),
                schedule.getStartsAt().format(DATE_FORMATTER),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                schedule.getLocationName(),
                stats.totalCount(),
                stats.presentCount(),
                stats.pendingCount(),
                stats.attendanceRate()
        );
    }

    private static String resolveStatus(Schedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        
        if (schedule.isEnded()) {
            return "종료됨";
        } else if (schedule.isInProgress()) {
            return "진행 중";
        } else {
            return "예정";
        }
    }
}
