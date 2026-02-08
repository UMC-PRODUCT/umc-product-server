package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "스터디 그룹 일정 생성 요청")
public record CreateStudyGroupScheduleRequest(
    @Schema(description = "일정 제목", example = "스터디 정기 모임")
    @NotBlank(message = "일정 제목은 필수입니다")
    String name,

    @Schema(description = "시작 일시", example = "2026-03-16T10:00:00")
    @NotNull(message = "시작 일시는 필수입니다")
    LocalDateTime startsAt,

    @Schema(description = "종료 일시", example = "2026-03-16T12:00:00")
    @NotNull(message = "종료 일시는 필수입니다")
    LocalDateTime endsAt,

    @Schema(description = "종일 여부", example = "false")
    boolean isAllDay,

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

    @Schema(description = "태그 목록", example = "[\"STUDY\"]")
    @NotNull(message = "태그는 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 태그를 선택해야 합니다")
    Set<ScheduleTag> tags,

    @Schema(description = "스터디 그룹 ID", example = "1")
    @NotNull(message = "스터디 그룹 ID는 필수입니다")
    Long studyGroupId,

    @Schema(description = "기수 ID", example = "1")
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval
) {
    public CreateStudyGroupScheduleCommand toCommand(Long authorMemberId) {
        return new CreateStudyGroupScheduleCommand(
            name,
            startsAt,
            endsAt,
            isAllDay,
            locationName,
            GeometryUtils.createPoint(latitude, longitude),
            description,
            tags,
            studyGroupId,
            gisuId,
            requiresApproval,
            authorMemberId
        );
    }
}
