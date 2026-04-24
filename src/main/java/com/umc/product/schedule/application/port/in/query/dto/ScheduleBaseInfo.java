package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;

// 범용적으로 쓰일 일정 정보를 담은 dto
public record ScheduleBaseInfo(
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    Long authorMemberId,
    Instant startsAt,
    Instant endsAt,

    // 위치 관련
    boolean isOnline,   // 비대면 일정인지 여부, 즉 location이 null인지 여부를 나타냄
    ScheduleLocationInfo location,

    // 출석 관련
    boolean isAttendanceChecked,    // 출석을 체크하는 일정인지, 즉 attendancePolicy의 null 여부를 나타냄
    ScheduleAttendancePolicyInfo attendancePolicy
) {
    public record ScheduleLocationInfo(
        Double latitude,
        Double longitude,
        String locationName
    ) {
    }

    public record ScheduleAttendancePolicyInfo(
        Instant checkInStartAt,
        Instant onTimeEndAt,
        Instant lateEndAt
    ) {
    }
}
