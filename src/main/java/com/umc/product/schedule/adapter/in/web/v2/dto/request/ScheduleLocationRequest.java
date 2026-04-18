package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScheduleLocationRequest(
    @Schema(description = "위도 (Latitude)", example = "37.498095")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    Double latitude,

    @Schema(description = "경도 (Longitude)", example = "127.027610")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    Double longitude,

    @Schema(description = "장소", example = "강남역 스터디룸")
    String locationName
) {
}
