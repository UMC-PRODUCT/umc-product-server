package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * 출석 정책 요청 DTO
 * <p>
 * 이 객체가 제공되면 모든 필드는 필수이며, 시간 순서(checkInStartAt < onTimeEndAt < lateEndAt)가 보장되어야 합니다. startsAt, endsAt와의 관계 검증은 도메인
 * 레이어에서 수행됩니다.
 */
public record ScheduleAttendancePolicyRequest(
    @Schema(description = "출석 요청 시작 일시 (UTC ISO8601. 예: 2026-05-21T00:40:00Z)", example = "2026-05-21T00:40:00Z")
    @NotNull(message = "출석 요청 시작 일시는 필수입니다")
    Instant checkInStartAt, // 출석 요청 시작 가능 시점

    @Schema(description = "출석 인정 마감 일시 (UTC ISO8601. 예: 2026-05-21T01:20:00Z)", example = "2026-05-21T01:20:00Z")
    @NotNull(message = "출석 인정 마감 일시는 필수입니다")
    Instant onTimeEndAt, // 출석으로 인정하는 마감 시간

    @Schema(description = "지각 인정 마감 일시 (UTC ISO8601. 예: 2026-05-21T02:00:00Z)", example = "2026-05-21T02:00:00Z")
    @NotNull(message = "지각 인정 마감 일시는 필수입니다")
    Instant lateEndAt // 지각으로 인정하는 마감 시간
) {
    // 검증해야 하는 내용: checkInStartAt < startAt < onTimeEndAt < lateEndAt < endsAt
    // 이 중 checkInStartAt < onTimeEndAt < lateEndAt는 여기서 검증
    // startsAt, endsAt와의 관계는 도메인에서 검증

    @AssertTrue(message = "출석 정책 시간 순서가 올바르지 않습니다 (checkInStartAt < onTimeEndAt < lateEndAt)")
    @Schema(hidden = true)
    public boolean isValidTimeOrder() {
        if (checkInStartAt == null || onTimeEndAt == null || lateEndAt == null) {
            return true; // @NotNull에서 처리
        }
        return checkInStartAt.isBefore(onTimeEndAt) && onTimeEndAt.isBefore(lateEndAt);
    }
}
