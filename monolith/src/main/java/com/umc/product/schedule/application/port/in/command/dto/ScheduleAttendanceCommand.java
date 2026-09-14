package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.Builder;

@Builder
public record ScheduleAttendanceCommand(
    Long scheduleId,

    // 요청자 memberId
    Long requesterMemberId,

    // 클라이언트 측에서 받은 위치 인증 여부
    Boolean locationVerified,

    // === 위치 정보 ===
    Double latitude,
    Double longitude
) {

    // 객체가 생성될 때 locationVerified가 null이면 에러 반환
    public ScheduleAttendanceCommand {
        if (locationVerified == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.LOCATION_NOT_VERIFIED);
        }
    }
}
