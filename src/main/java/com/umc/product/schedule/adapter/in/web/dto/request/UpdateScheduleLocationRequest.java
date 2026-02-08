package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "일정 출석체크 위치 변경")
public record UpdateScheduleLocationRequest(
    @Schema(description = "장소", example = "강남역 스터디룸")
    @NotBlank
    String locationName,

    @Schema(description = "위도 (Latitude)", example = "37.498095")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    @NotBlank
    Double latitude,

    @Schema(description = "경도 (Longitude)", example = "127.027610")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    @NotBlank
    Double longitude
) {
    public UpdateScheduleLocationCommand toCommand(Long scheduleId) {
        return UpdateScheduleLocationCommand.of(
            scheduleId,
            locationName,
            GeometryUtils.createPoint(latitude, longitude)
        );
    }
}
