package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Schema(description = "일정 수정 요청 (변경할 필드만 보내주세요)")
public record UpdateScheduleRequest(

        @Schema(description = "일정 제목", example = "9기 OT")
        String name,

        @Schema(description = "시작 일시", example = "2026-03-16T10:00:00")
        LocalDateTime startsAt,

        @Schema(description = "종료 일시", example = "2026-03-16T12:00:00")
        LocalDateTime endsAt,

        @Schema(description = "종일 여부", example = "false")
        Boolean isAllDay,

        @Schema(description = "장소", example = "강남역 스터디룸")
        String locationName,

        @Schema(description = "위도 (Latitude)", example = "37.498095")
        @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
        @Max(value = 90, message = "위도는 90 이하여야 합니다")
        Double latitude,

        @Schema(description = "경도 (Longitude)", example = "127.027610")
        @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
        @Max(value = 180, message = "경도는 180 이하여야 합니다")
        Double longitude,

        @Schema(description = "메모/설명")
        String description,

        @Schema(description = "카테고리", example = "TEAM_ACTIVITY")
        ScheduleType scheduleType
) {
    public UpdateScheduleCommand toCommand(Long scheduleId) {
        Point point = createPoint(latitude, longitude);
        return UpdateScheduleCommand.of(
                scheduleId,
                name,
                startsAt,
                endsAt,
                isAllDay,
                locationName,
                point,
                description,
                scheduleType
        );
    }

    // 좌표 변환 헬퍼 메서드
    private Point createPoint(Double lat, Double lon) {
        if (lat == null || lon == null) {
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}
