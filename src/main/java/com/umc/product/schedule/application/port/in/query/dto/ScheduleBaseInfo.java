package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.AttendancePolicy;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
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

    public static ScheduleBaseInfo from(Schedule schedule) {
        ScheduleBaseInfo scheduleBaseInfo = ScheduleBaseInfo.builder()
            .scheduleId(schedule.getId())
            .name(schedule.getName())
            .description(schedule.getDescription())
            .tags(schedule.getTags())
            .authorMemberId(schedule.getAuthorMemberId())
            .startsAt(schedule.getStartsAt())
            .endsAt(schedule.getEndsAt())
            .isOnline(schedule.getLocation() == null)
            .location(mapLocation(schedule))
            .isAttendanceChecked(schedule.getPolicy() != null)
            .attendancePolicy(mapPolicy(schedule))
            .build();
        return scheduleBaseInfo;
    }

    // ScheduleLocationInfo 변환 헬퍼 메서드
    private static ScheduleLocationInfo mapLocation(Schedule schedule) {
        if (schedule.getLocation() == null) {
            return null;
        }

        return ScheduleLocationInfo.builder()
            .latitude(schedule.getLocation().getY())
            .longitude(schedule.getLocation().getX())
            .locationName(schedule.getLocationName())
            .build();
    }

    // ScheduleAttendancePolicyInfo 변환 헬퍼 메서드
    private static ScheduleAttendancePolicyInfo mapPolicy(Schedule schedule) {
        AttendancePolicy policy = schedule.getPolicy();
        if (policy == null) {
            return null;
        }

        Instant start = schedule.getStartsAt();

        // 정책을 따른 시간 계산
        Instant checkInStart = start.minus(policy.getEarlyCheckInMinutes(), ChronoUnit.MINUTES);
        Instant onTimeEnd = start.plus(policy.getAttendanceGraceMinutes(), ChronoUnit.MINUTES);
        Instant lateEnd = onTimeEnd.plus(policy.getLateToleranceMinutes(), ChronoUnit.MINUTES);

        return ScheduleAttendancePolicyInfo.builder()
            .checkInStartAt(checkInStart)
            .onTimeEndAt(onTimeEnd)
            .lateEndAt(lateEnd)
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ScheduleLocationInfo(
        Double latitude,
        Double longitude,
        String locationName
    ) {
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ScheduleAttendancePolicyInfo(
        Instant checkInStartAt,
        Instant onTimeEndAt,
        Instant lateEndAt
    ) {
    }
}
