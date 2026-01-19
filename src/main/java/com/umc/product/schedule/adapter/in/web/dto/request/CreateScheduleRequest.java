package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 생성 요청")
public record CreateScheduleRequest(

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

        @Schema(description = "메모/설명")
        String description,

        @Schema(description = "참여자 Member ID 목록")
        List<Long> participantMemberIds,

        @Schema(description = "카테고리", example = "TEAM_ACTIVITY")
        @NotNull(message = "카테고리는 필수입니다")
        ScheduleType scheduleType
) {
    public CreateScheduleCommand toCommand(Long authorMemberId) {
        return CreateScheduleCommand.of(
                name,
                startsAt,
                endsAt,
                isAllDay,
                locationName,
                description,
                participantMemberIds,
                scheduleType,
                authorMemberId
        );
    }
}
