package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.application.port.v2.in.command.dto.ExcuseScheduleAttendanceCommand;

public record ExcuseScheduleAttendanceRequest(
    // 클라이언트 측에서 받은 위치 인증 여부
    boolean isVerified,

    // === 위치 정보, nullable ===
    Double latitude,
    Double longitude,

    // 사유, 필수 값
    String excuseReason
) {

    public ExcuseScheduleAttendanceCommand toCommand(Long scheduleId, Long requesterMemberId) {
        return ExcuseScheduleAttendanceCommand.builder()
            .scheduleId(scheduleId)
            .requesterMemberId(requesterMemberId)
            .isVerified(isVerified)
            .latitude(latitude)
            .longitude(longitude)
            .excuseReason(excuseReason)
            .build();
    }
}
