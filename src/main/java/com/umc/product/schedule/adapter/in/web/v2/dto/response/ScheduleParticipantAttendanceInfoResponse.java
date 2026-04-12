package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;

public record ScheduleParticipantAttendanceInfoResponse(
    // === 위치 정보 ===
    Double latitude,
    Double longitude,
    AttendanceStatus status,
    // AttendanceStatus에 따라서, 출석이 운영진의 승인을 기다리고 있는지를 반환합니다.
    // decidedMemberInfo의 null 여부와는 무관하므로 해당 여부에 대한 flag 값으로 사용하지 않도록 유의합니다.
    boolean isPendingDecision,

    // === 출석 상태 및 승인/기각한 사람 정보 ===

    // decidedMemberInfo의 null 여부에 대한 flag 값입니다.
    // isPendingDecision과의 혼동을 방지하기 위해서 존재합니다.
    boolean hasDecidedMember,
    DecidedMemberInfo decidedMemberInfo,
    Instant decidedAt,
    String decisionReason
) {
    public record DecidedMemberInfo(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName
    ) {
    }
}
