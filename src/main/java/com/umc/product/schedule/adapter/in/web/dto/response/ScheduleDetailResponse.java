package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "일정 상세 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduleDetailResponse(
    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String name,

    @Schema(description = "설명", example = "9기 오리엔테이션입니다.")
    String description,

    @Schema(description = "태그 목록", example = "[\"STUDY\", \"PROJECT\"]")
    Set<String> tags,

    @Schema(description = "시작 일시", example = "2026-03-16T10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant startsAt,

    @Schema(description = "종료 일시", example = "2026-03-16T12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant endsAt,

    @Schema(description = "종일 여부", example = "false")
    boolean isAllDay,

    @Schema(description = "장소명", example = "강남역 스터디룸")
    String locationName,

    @Schema(description = "위도", example = "37.498095")
    Double latitude,

    @Schema(description = "경도", example = "127.027610")
    Double longitude,

    @Schema(description = "일정 상태", example = "예정")
    String status,

    @Schema(description = "D-Day", example = "7")
    long dDay,

    @Schema(description = "출석 승인 필요 여부", example = "true")
    boolean requiresAttendanceApproval
) {

    public static ScheduleDetailResponse from(ScheduleDetailInfo info) {
        Set<String> tagNames = info.tags() != null
            ? info.tags().stream().map(Enum::name).collect(Collectors.toSet())
            : Set.of();

        return new ScheduleDetailResponse(
            info.scheduleId(),
            info.name(),
            info.description(),
            tagNames,
            info.startsAt(),
            info.endsAt(),
            info.isAllDay(),
            info.locationName(),
            info.latitude(),
            info.longitude(),
            info.status(),
            info.requiresAttendanceApproval()
        );
    }
}
