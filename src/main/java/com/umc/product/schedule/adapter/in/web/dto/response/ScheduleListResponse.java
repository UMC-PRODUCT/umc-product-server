package com.umc.product.schedule.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

// TODO : 주석 처리 부분 tags 로 변경
@Schema(description = "일정 목록 응답")
public record ScheduleListResponse(
    @Schema(description = "일정 ID", example = "1")
    Long scheduleId,

    @Schema(description = "일정명", example = "9기 OT")
    String name,

//        String type,

    @Schema(description = "일정 상태 (진행 중, 종료됨)", example = "진행 중")
    String status,

    @Schema(description = "날짜", example = "2026-03-16T01:00:00Z")
    Instant date,

    @Schema(description = "시작 시간", example = "2026-03-16T01:00:00Z")
    Instant startTime,

    @Schema(description = "종료 시간", example = "2026-03-16T03:00:00Z")
    Instant endTime,

    @Schema(description = "장소명", example = "강남역 스터디룸")
    String locationName,

    @Schema(description = "전체 인원", example = "30")
    Integer totalCount,

    @Schema(description = "출석 인원", example = "25")
    Integer presentCount,

    @Schema(description = "대기 인원", example = "3")
    Integer pendingCount,

    @Schema(description = "출석률", example = "80")
    Double attendanceRate
) {
}
