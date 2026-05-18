package com.umc.product.schedule.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record ExcuseScheduleAttendanceCommand(

    Long scheduleId,

    // 요청자 memberId
    Long requesterMemberId,

    // 클라이언트 측에서 받은 위치 인증 여부
    boolean isVerified,

    // === 위치 정보, nullable ===
    Double latitude,
    Double longitude,

    // 사유, 필수 값
    String excuseReason
) {
}
