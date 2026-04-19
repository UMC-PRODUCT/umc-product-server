package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse.ScheduleAttendancePolicyInfoResponse;
import com.umc.product.schedule.adapter.in.web.v2.dto.response.ScheduleInfoResponse.ScheduleLocationInfoResponse;
import com.umc.product.schedule.application.port.v2.in.query.dto.AdminScheduleInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.AdminScheduleInfo.AdminScheduleParticipantInfo;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleBaseInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record AdminScheduleInfoResponse(
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    Long authorMemberId,
    Instant startsAt,
    Instant endsAt,

    // 위치 관련
    boolean isOnline, // 비대면 일정인지 여부, 즉 location이 null인지 여부를 나타냄
    ScheduleLocationInfoResponse location,

    ScheduleAttendancePolicyInfoResponse attendancePolicy,

    List<AdminScheduleParticipantInfoResponse> participants

) {

    public static AdminScheduleInfoResponse from(AdminScheduleInfo info) {
        ScheduleBaseInfo base = info.baseInfo();

        return AdminScheduleInfoResponse.builder()
            .scheduleId(base.scheduleId())
            .name(base.name())
            .description(base.description())
            .tags(base.tags())
            .authorMemberId(base.authorMemberId())
            .startsAt(base.startsAt())
            .endsAt(base.endsAt())
            .isOnline(base.isOnline())
            .location(ScheduleLocationInfoResponse.from(base.location()))
            .attendancePolicy(ScheduleAttendancePolicyInfoResponse.from(base.attendancePolicy()))
            .participants(
                info.participants().stream()
                    .map(AdminScheduleParticipantInfoResponse::from)
                    .toList()
            )
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record AdminScheduleParticipantInfoResponse(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName,
        String profileImageUrl,

        // 운영진만 볼 수 있는 추가 데이터
        AttendanceStatus attendanceStatus,
        boolean isLocationVerified,
        String excuseReason
    ) {
        public static AdminScheduleParticipantInfoResponse from(AdminScheduleParticipantInfo info) {
            return AdminScheduleParticipantInfoResponse.builder()
                .memberId(info.memberId())
                .name(info.name())
                .nickname(info.nickname())
                .schoolId(info.schoolId())
                .schoolName(info.schoolName())
                .profileImageUrl(info.profileImageUrl())
                .attendanceStatus(info.attendanceStatus())
                .isLocationVerified(info.isLocationVerified())
                .excuseReason(info.excuseReason())
                .build();
        }
    }
}
