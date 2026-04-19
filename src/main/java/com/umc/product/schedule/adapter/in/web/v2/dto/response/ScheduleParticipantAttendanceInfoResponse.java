package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.application.port.v2.in.command.dto.result.ScheduleParticipantAttendanceResult;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ScheduleParticipantAttendanceInfoResponse(
    // === 위치 정보 ===
    Double latitude,
    Double longitude,
    AttendanceStatus status,
    String excuseReason, // 사유를 제출한 경우, 해당 사유입니다. nullable!
    // AttendanceStatus에 따라서, 출석이 운영진의 승인을 기다리고 있는지를 반환합니다.
    // decidedMemberInfo의 null 여부와는 무관하므로 해당 여부에 대한 flag 값으로 사용하지 않도록 유의합니다.
    boolean isPendingDecision,

    // === 출석 상태 및 승인/기각한 사람 정보 ===

    // DecisionMakerMemberInfo의 null 여부에 대한 flag 값입니다.
    // isPendingDecision과의 혼동을 방지하기 위해서 존재합니다.
    boolean hasDecisionMakerMember,
    DecisionMakerMemberInfo decisionMakerMemberInfo,
    Instant decidedAt,
    String decisionReason
) {
    public static ScheduleParticipantAttendanceInfoResponse from(ScheduleParticipantAttendanceResult info) {
        if (info == null) {
            return null;
        }

        return ScheduleParticipantAttendanceInfoResponse.builder()
            .latitude(info.latitude())
            .longitude(info.longitude())
            .status(info.status())
            .excuseReason(info.excuseReason())
            .isPendingDecision(info.isPendingDecision())
            .hasDecisionMakerMember(info.hasDecisionMakerMember())
            .decisionMakerMemberInfo(DecisionMakerMemberInfo.from(info))
            .decidedAt(info.decidedAt())
            .decisionReason(info.decisionReason())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record DecisionMakerMemberInfo(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName
    ) {

        public static DecisionMakerMemberInfo from(ScheduleParticipantAttendanceResult info) {
            if (info == null || !info.hasDecisionMakerMember()) {
                return null;
            }

            return DecisionMakerMemberInfo.builder()
                .memberId(info.decisionMakerMemberInfo().memberId())
                .name(info.decisionMakerMemberInfo().name())
                .nickname(info.decisionMakerMemberInfo().nickname())
                .schoolId(info.decisionMakerMemberInfo().schoolId())
                .schoolName(info.decisionMakerMemberInfo().schoolName())
                .build();
        }
    }
}
