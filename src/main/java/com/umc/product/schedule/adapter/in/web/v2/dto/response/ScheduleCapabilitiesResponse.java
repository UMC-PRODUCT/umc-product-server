package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleCapabilitiesInfo;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ScheduleCapabilitiesResponse(
    boolean canCreateSchedule,
    boolean canCreateAttendanceRequiredSchedule,
    int maxParticipantCount
) {

    public static ScheduleCapabilitiesResponse from(ScheduleCapabilitiesInfo info) {
        return ScheduleCapabilitiesResponse.builder()
            .canCreateSchedule(info.canCreateSchedule())
            .canCreateAttendanceRequiredSchedule(info.canCreateAttendanceRequiredSchedule())
            .maxParticipantCount(info.maxParticipantCount())
            .build();
    }
}
