package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 일정 응답")
public record MyScheduleResponse(
    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String name,

    @Schema(description = "시작 일시", example = "2026-03-16T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startsAt,

    @Schema(description = "종료 일시", example = "2026-03-16T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endsAt,

    @Schema(description = "일정 상태", example = "UPCOMING")
    String status,

    @Schema(description = "D-Day", example = "7")
    long dDay
) {

    public static MyScheduleResponse from(MyScheduleInfo info) {
        return new MyScheduleResponse(
            info.scheduleId(),
            info.name(),
            info.startsAt(),
            info.endsAt(),
            info.status(),
            info.dDay()
        );
    }
}
