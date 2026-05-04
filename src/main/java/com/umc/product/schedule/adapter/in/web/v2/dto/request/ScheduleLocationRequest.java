package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 일정 위치 요청 DTO
 * <p>
 * 이 객체가 제공되면 모든 필드는 필수입니다. (대면 일정으로 간주)
 */
public record ScheduleLocationRequest(
    @Schema(description = "위도 (Latitude)", example = "37.498095")
    @NotNull(message = "위도는 필수입니다")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    Double latitude,

    @Schema(description = "경도 (Longitude)", example = "127.027610")
    @NotNull(message = "경도는 필수입니다")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    Double longitude,

    @Schema(description = "장소", example = "강남역 스터디룸", maxLength = 100)
    @NotBlank(message = "장소명은 필수입니다")
    @Size(max = 100, message = "일정의 장소명은 최대 100자까지 입력 가능합니다.")
    String locationName
) {
}
