package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.application.port.v2.in.command.dto.DecideAttendanceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 각 출석 요청에 대해서 승인 및 거절 여부를 결정합니다.
 *
 * @param participantMemberId 어떤 사용자의 출석 요청에 대한 것인지를 나타냅니다.
 * @param isApproved          승인 여부입니다. true인 경우 승인, false인 경우 거절로 간주됩니다.
 * @param reason              승인 또는 거절 사유가 있다면 제공합니다. nullable!
 */
public record DecideAttendanceRequest(
    @Schema(description = "참석자 memberId", example = "1")
    @NotNull(message = "출석 요청을 처리할 참석자 memberId는 필수입니다.")
    @Min(value = 1, message = "1 이상인 수여야 합니다.")
    Long participantMemberId,

    @Schema(description = "출석 요청 승인 여부", example = "true")
    @NotNull(message = "승인 여부는 필수입니다.")
    boolean isApproved,

    String reason
) {
    public DecideAttendanceCommand toCommand(Long scheduleId, Long decisionMakerMemberId) {
        return DecideAttendanceCommand.builder()
            .scheduleId(scheduleId)
            .decisionMakerMemberId(decisionMakerMemberId)
            .participantMemberId(participantMemberId)
            .isApproved(isApproved)
            .reason(reason)
            .build();
    }
}
