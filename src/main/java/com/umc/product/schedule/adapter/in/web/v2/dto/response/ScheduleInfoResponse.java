package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleBaseInfo.ScheduleAttendancePolicyInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleBaseInfo.ScheduleLocationInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleInfo.ScheduleParticipantInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ScheduleInfoResponse(
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

    // 출석 관련
    // 해당 일정에 대한 요청자의 출석 상태입니다. 요청한 사람이 참여자가 아니거나 아직 출석 요청을 하지 않은 경우 null로 갑니다.
    AttendanceStatus attendanceStatus,
    boolean isAttendanceChecked, // 출석을 체크하는 일정인지, 즉 attendancePolicy의 null 여부를 나타냄
    ScheduleAttendancePolicyInfoResponse attendancePolicy,

    // 참여자 관련
    boolean isParticipant, // 요청한 사용자가 해당 일정의 참여자인지 여부입니다. 참여자 목록에 요청한 사용자가 포함되어 있는지 여부와는 별개로, 서버 측에서 별도로 계산해서 제공합니다.
    List<ScheduleParticipantInfoResponse> participants
) {
    public static ScheduleInfoResponse from(ScheduleInfo info) {

        return ScheduleInfoResponse.builder()
            .scheduleId(info.baseInfo().scheduleId())
            .name(info.baseInfo().name())
            .description(info.baseInfo().description())
            .tags(info.baseInfo().tags())
            .authorMemberId(info.baseInfo().authorMemberId())
            .startsAt(info.baseInfo().startsAt())
            .endsAt(info.baseInfo().endsAt())
            .isOnline(info.baseInfo().isOnline())
            .location(ScheduleLocationInfoResponse.from(info.baseInfo().location()))
            .attendanceStatus(info.attendanceStatus())
            .isAttendanceChecked(info.baseInfo().isAttendanceChecked())
            .attendancePolicy(ScheduleAttendancePolicyInfoResponse.from(info.baseInfo().attendancePolicy()))
            .isParticipant(info.isParticipant())
            .participants(
                info.participants().stream()
                    .map(ScheduleParticipantInfoResponse::from)
                    .toList()
            )
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ScheduleLocationInfoResponse(
        Double latitude,
        Double longitude,
        String locationName
    ) {
        public static ScheduleLocationInfoResponse from(ScheduleLocationInfo info) {
            if (info == null) {
                return null;
            }

            return ScheduleLocationInfoResponse.builder()
                .latitude(info.latitude())
                .longitude(info.longitude())
                .locationName(info.locationName())
                .build();
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ScheduleAttendancePolicyInfoResponse(
        // Client단을 위해서, 서버 측에서 earlyCheckInMinutes, lateCheckInMinutes, lateToleranceMinutes를 기반으로
        // 아래 3가지 값을 계산해서 제공하도록 합니다.
        Instant checkInStartAt,
        Instant onTimeEndAt,
        Instant lateEndAt
    ) {

        public static ScheduleAttendancePolicyInfoResponse from(ScheduleAttendancePolicyInfo info) {
            if (info == null) {
                return null;
            }

            return ScheduleAttendancePolicyInfoResponse.builder()
                .checkInStartAt(info.checkInStartAt())
                .onTimeEndAt(info.onTimeEndAt())
                .lateEndAt(info.lateEndAt())
                .build();
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ScheduleParticipantInfoResponse(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName,
        String profileImageUrl
    ) {
        public static ScheduleParticipantInfoResponse from(ScheduleParticipantInfo info) {
            if (info == null) {
                return null;
            }
            return ScheduleParticipantInfoResponse.builder()
                .memberId(info.memberId())
                .name(info.name())
                .nickname(info.nickname())
                .schoolId(info.schoolId())
                .schoolName(info.schoolName())
                .profileImageUrl(info.profileImageUrl())
                .build();
        }
    }
}
