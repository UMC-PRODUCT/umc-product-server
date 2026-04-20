package com.umc.product.schedule.application.port.in.command.dto;

import lombok.Builder;

/**
 * @param scheduleId            출석 요청을 승인/거절하는 일정을 나타냅니다.
 * @param decisionMakerMemberId 출석 요청을 승인/거절하는 사용자를 나타냅니다.
 * @param participantMemberId   어떤 사용자의 출석 요청에 대한 것인지를 나타냅니다.
 * @param isApproved            승인 여부입니다. true인 경우 승인, false인 경우 거절로 간주됩니다.
 * @param reason                승인 또는 거절 사유가 있다면 제공합니다. nullable!
 */
@Builder
public record DecideAttendanceCommand(
    Long scheduleId,
    Long decisionMakerMemberId,
    Long participantMemberId,
    boolean isApproved,
    String reason
) {
}
