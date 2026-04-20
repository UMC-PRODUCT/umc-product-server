package com.umc.product.schedule.application.port.in.command.dto.result;

import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.schedule.domain.ScheduleParticipantAttendance;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

@Builder(access = AccessLevel.PRIVATE)
public record ScheduleParticipantAttendanceResult(
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
    public static ScheduleParticipantAttendanceResult of(
        ScheduleParticipantAttendance attendance,
        MemberInfo decisionMakerMemberInfo // nullable!
    ) {
        if (attendance == null) {
            return null;
        }

        Point location = attendance.getLocation();

        return ScheduleParticipantAttendanceResult.builder()
            .latitude(location != null ? location.getY() : null)
            .longitude(location != null ? location.getX() : null)
            .status(attendance.getStatus())
            .excuseReason(attendance.getExcuseReason())
            .isPendingDecision(attendance.getStatus().isPending())
            .hasDecisionMakerMember(decisionMakerMemberInfo != null)
            .decisionMakerMemberInfo(
                decisionMakerMemberInfo != null ?
                    DecisionMakerMemberInfo.from(decisionMakerMemberInfo)
                    : null
            )
            .decidedAt(attendance.getDecidedAt())
            .decisionReason(attendance.getDecisionReason())
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

        public static DecisionMakerMemberInfo from(MemberInfo memberInfo) {
            if (memberInfo == null) {
                return null;
            }
            return DecisionMakerMemberInfo.builder()
                .memberId(memberInfo.id())
                .name(memberInfo.name())
                .nickname(memberInfo.nickname())
                .schoolId(memberInfo.schoolId())
                .schoolName(memberInfo.schoolName())
                .build();
        }
    }
}
