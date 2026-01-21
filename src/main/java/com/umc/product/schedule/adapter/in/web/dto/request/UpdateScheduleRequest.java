package com.umc.product.schedule.adapter.in.web.dto.request;

import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

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

        @Schema(description = "메모/설명")
        String description,

        @Schema(description = "카테고리", example = "TEAM_ACTIVITY")
        ScheduleType scheduleType
) {
    public UpdateScheduleCommand toCommand(Long scheduleId) {
        return UpdateScheduleCommand.of(
                scheduleId,
                name,
                startsAt,
                endsAt,
                isAllDay,
                locationName,
                description,
                scheduleType
        );
    }
}
