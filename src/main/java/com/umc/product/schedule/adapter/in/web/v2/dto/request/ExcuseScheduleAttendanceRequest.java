package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.ExcuseScheduleAttendanceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExcuseScheduleAttendanceRequest(
    // 클라이언트 측에서 받은 위치 인증 여부
    @Schema(description = "(필수 값) 클라이언트 측의 위치 인증 여부, 비대면 일정일 경우 false", example = "true")
    boolean isVerified,

    // === 위치 정보, nullable ===
    @Schema(description = "위도 (Latitude)", example = "37.498095")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    Double latitude,

    @Schema(description = "경도 (Longitude)", example = "127.027610")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    Double longitude,

    // 사유, 필수 값
    @Schema(description = "사유 출석 사유", example = "위치 인증이 안 되어서 사유 출석 제출합니다.", maxLength = 300)
    @NotBlank(message = "사유 작성은 필수입니다")
    @Size(max = 300, message = "사유는 300자까지 입력 가능합니다")
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
