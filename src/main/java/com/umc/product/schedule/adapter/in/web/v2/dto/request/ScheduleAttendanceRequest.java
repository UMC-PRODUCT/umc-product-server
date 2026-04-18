package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.application.port.v2.in.command.dto.ScheduleAttendanceCommand;

public record ScheduleAttendanceRequest(
    // 클라이언트 측에서 받은 위치 인증 여부
    boolean isVerified,
    // === 위치 정보 ===
    Double latitude,
    Double longitude
) {

    public ScheduleAttendanceCommand toCommand(Long scheduleId, Long requesterMemberId) {
        return ScheduleAttendanceCommand.builder()
            .scheduleId(scheduleId)
            .requesterMemberId(requesterMemberId)
            .isVerified(isVerified)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }
}
