package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleType;
import com.umc.product.schedule.domain.vo.AttendanceStats;
import java.time.LocalDateTime;

public record ScheduleWithStatsInfo(
        // === Schedule Info ===
        Long scheduleId,
        String name,
        ScheduleType type,
        String status,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String locationName,
        // === Attendance Stats ===
        Integer totalCount,
        Integer presentCount,
        Integer pendingCount,
        Double attendanceRate
) {
    /**
     * Schedule 엔티티와 통계 수치를 받아 Info DTO 생성
     */
    public static ScheduleWithStatsInfo of(Schedule schedule, AttendanceStats stats, LocalDateTime now) {
        return new ScheduleWithStatsInfo(
                schedule.getId(),
                schedule.getName(),
                schedule.getType(),
                schedule.resolveStatus(now),
                schedule.getStartsAt(),
                schedule.getEndsAt(),
                schedule.getLocationName(),
                stats.totalCount(),
                stats.presentCount(),
                stats.pendingCount(),
                stats.calculateAttendanceRate()
        );
    }
}

