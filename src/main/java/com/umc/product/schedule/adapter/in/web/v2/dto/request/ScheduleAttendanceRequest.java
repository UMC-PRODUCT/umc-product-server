package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umc.product.schedule.application.port.in.command.dto.ScheduleAttendanceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScheduleAttendanceRequest(
    // 클라이언트 측에서 받은 위치 인증 여부
    @Schema(description = "클라이언트 측의 위치 인증 여부, 비대면 일정일 경우 false", example = "true")
    @NotNull(message = "위치 인증 여부는 필수입니다.")
    @JsonProperty("isVerified")
    boolean isVerified,

    // === 위치 정보 ===
    @Schema(description = "위도 (Latitude)", example = "37.498095")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    Double latitude,

    @Schema(description = "경도 (Longitude)", example = "127.027610")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
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
