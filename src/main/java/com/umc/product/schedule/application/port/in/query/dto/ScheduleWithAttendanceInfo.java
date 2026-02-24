package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;

/**
 * Schedule + AttendanceSheet 통합 정보 DTO (Facade)
 */
public record ScheduleWithAttendanceInfo(
    // Schedule 정보
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    Instant startsAt,
    Instant endsAt,
    boolean isAllDay,
    String locationName,
    Double latitude,
    Double longitude,
    String status,
    long dDay,
    // AttendanceSheet 정보
    AttendanceSheetInfo attendanceSheet
) {

    /**
     * ScheduleDetailInfo와 AttendanceSheetInfo를 조합하여 생성
     *
     * @param scheduleInfo   일정 상세 정보
     * @param attendanceInfo 출석부 정보 (null 가능 - 참여자가 없는 일정)
     */
    public static ScheduleWithAttendanceInfo of(
        ScheduleDetailInfo scheduleInfo,
        AttendanceSheetInfo attendanceInfo
    ) {
        return new ScheduleWithAttendanceInfo(
            scheduleInfo.scheduleId(),
            scheduleInfo.name(),
            scheduleInfo.description(),
            scheduleInfo.tags(),
            scheduleInfo.startsAt(),
            scheduleInfo.endsAt(),
            scheduleInfo.isAllDay(),
            scheduleInfo.locationName(),
            scheduleInfo.latitude(),
            scheduleInfo.longitude(),
            scheduleInfo.status(),
            scheduleInfo.dDay(),
            attendanceInfo
        );
    }
}
