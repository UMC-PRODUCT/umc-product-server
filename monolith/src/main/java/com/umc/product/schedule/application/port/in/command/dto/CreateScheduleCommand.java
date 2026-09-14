package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

@Builder
public record CreateScheduleCommand(
    String name,
    String description,
    Set<ScheduleTag> tags,
    Long authorMemberId,
    Instant startsAt,
    Instant endsAt,
    LocationInfo location,
    AttendancePolicyInfo attendancePolicy,
    Set<Long> participantMemberIds
) {
    public Schedule toEntity(Long authorMemberId) {
        return Schedule.builder()
            .name(this.name)
            .description(this.description)
            .tags(this.tags)
            .authorMemberId(authorMemberId)
            .startsAt(this.startsAt)
            .endsAt(this.endsAt)
            .locationName(location != null ? location.locationName() : null)
            .location(
                location != null ?
                    GeometryUtils.createPoint(location().latitude(), location().longitude())
                    : null)
            .policy(
                attendancePolicy != null ? Schedule.createAttendancePolicy(
                    attendancePolicy.checkInStartAt(),
                    attendancePolicy.onTimeEndAt(),
                    attendancePolicy.lateEndAt(),
                    this.startsAt,
                    this.endsAt
                ) : null)
            .build();
    }

    @Builder
    public record LocationInfo(
        Double latitude,
        Double longitude,
        String locationName
    ) {
    }

    @Builder
    public record AttendancePolicyInfo(
        Instant checkInStartAt,
        Instant onTimeEndAt,
        Instant lateEndAt
    ) {
    }
}
