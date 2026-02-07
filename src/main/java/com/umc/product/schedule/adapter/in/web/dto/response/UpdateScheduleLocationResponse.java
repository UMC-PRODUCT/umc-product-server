package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 출석체크 위치 변경 응답")
public record UpdateScheduleLocationResponse(
    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "장소명", example = "강남역 스터디룸")
    String locationName,

    @Schema(description = "위도", example = "37.498095")
    Double latitude,

    @Schema(description = "경도", example = "127.027610")
    Double longitude
) {
    public static UpdateScheduleLocationResponse from(UpdateScheduleLocationInfo info) {
        return new UpdateScheduleLocationResponse(
            info.scheduleId(),
            info.locationName(),
            info.latitude(),
            info.longitude()
        );
    }
}
