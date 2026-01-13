package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleType;
import com.umc.product.schedule.domain.vo.AttendanceStats;
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
    public static ScheduleWithStatsInfo of(Schedule schedule, AttendanceStats stats, LocalDateTime now) {
        return new ScheduleWithStatsInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getType(),
                schedule.resolveStatus(now),         // 1번 해결: 도메인에 위임
                schedule.getStartsAt().format(DATE_FORMATTER),
                schedule.getStartsAt().toLocalTime(),
                schedule.getEndsAt().toLocalTime(),
                schedule.getLocationName(),
                stats.totalCount(),
                stats.presentCount(),
                stats.pendingCount(),
                stats.calculateAttendanceRate()     // 2번 해결: VO에 위임
        );
    }
}

