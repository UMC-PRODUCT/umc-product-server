package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

@Schema(description = "일정 수정 요청 (변경할 필드만 보내주세요)")
public record UpdateScheduleRequest(

    @Schema(description = "일정 제목", example = "9기 OT")
    String name,

    @Schema(description = "시작 일시 (UTC)", example = "2026-03-16T01:00:00Z")
    @NotNull(message = "시작 일시는 필수입니다")
    Instant startsAt,

    @Schema(description = "종료 일시 (UTC)", example = "2026-03-16T03:00:00Z")
    @NotNull(message = "종료 일시는 필수입니다")
    Instant endsAt,

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

    @Schema(description = "태그 목록 (null이면 기존 태그 유지)", example = "[\"STUDY\", \"PROJECT\"]")
    @Size(min = 1, message = "태그를 수정하려면 최소 1개 이상 선택해야 합니다")
    Set<ScheduleTag> tags
) {
    public UpdateScheduleCommand toCommand(Long scheduleId) {
        return UpdateScheduleCommand.of(
            scheduleId,
            name,
            startsAt != null ? startsAt.atZone(KST).toInstant() : null,
            endsAt != null ? endsAt.atZone(KST).toInstant() : null,
            isAllDay,
            locationName,
            GeometryUtils.createPoint(latitude, longitude),
            description,
            tags
        );
    }
}
