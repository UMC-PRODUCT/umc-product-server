package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.vo.AttendanceStats;
import java.time.Instant;

// TODO : 주석 처리 부분 tags 로 변경
public record ScheduleWithStatsInfo(
    // === Schedule Info ===
    Long scheduleId,
    String name,
//        ScheduleType type,
    String status,
    Instant startsAt,
    Instant endsAt,
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
    public static ScheduleWithStatsInfo of(Schedule schedule, AttendanceStats stats, Instant now) {
        return new ScheduleWithStatsInfo(
            schedule.getId(),
            schedule.getName(),
//                schedule.getType(),
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
