package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleWithAttendanceCommand;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "일정 + 출석부 통합 생성 요청")
public record CreateScheduleWithAttendanceRequest(
    // Schedule 정보
    @Schema(description = "일정 제목", example = "9기 OT")
    @NotBlank(message = "일정 제목은 필수입니다")
    String name,

    @Schema(description = "시작 일시", example = "2026-03-16T10:00:00")
    LocalDateTime startsAt,

    @Schema(description = "종료 일시", example = "2026-03-16T12:00:00")
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

    @Schema(description = "참여자 Member ID 목록")
    List<Long> participantMemberIds,

    @Schema(description = "태그 목록", example = "[\"STUDY\", \"PROJECT\"]")
    @NotNull(message = "태그는 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 태그를 선택해야 합니다")
    Set<ScheduleTag> tags,

    // AttendanceSheet 정보
    @Schema(description = "출석 시작 시간", example = "2026-03-16T09:50:00")
    @NotNull(message = "출석 시작 시간은 필수입니다")
    LocalDateTime attendanceStartTime,

    @Schema(description = "출석 종료 시간", example = "2026-03-16T10:10:00")
    @NotNull(message = "출석 종료 시간은 필수입니다")
    LocalDateTime attendanceEndTime,

    @Schema(description = "지각 기준 시간(분)", example = "10")
    @NotNull(message = "지각 기준 시간은 필수입니다")
    Integer lateThresholdMinutes,

    @Schema(description = "승인 필요 여부", example = "true")
    boolean requiresApproval
) {
    public CreateScheduleWithAttendanceRequest {
        if (attendanceStartTime != null && attendanceEndTime != null
            && attendanceStartTime.isAfter(attendanceEndTime)) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    public CreateScheduleWithAttendanceCommand toCommand(Long authorMemberId) {
        AttendanceWindow window = AttendanceWindow.from(
            attendanceStartTime,
            attendanceEndTime,
            lateThresholdMinutes
        );

        return new CreateScheduleWithAttendanceCommand(
            name,
            startsAt,
            endsAt,
            isAllDay,
            locationName,
            GeometryUtils.createPoint(latitude, longitude),
            description,
            participantMemberIds,
            tags,
            authorMemberId,
            window,
            requiresApproval
        );
    }
}
