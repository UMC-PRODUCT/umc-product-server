package com.umc.product.schedule.adapter.in.web.v2.dto.response;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    boolean isAttendanceChecked, // 출석을 체크하는 일정인지, 즉 attendancePolicy의 null 여부를 나타냄
    ScheduleAttendancePolicyInfoResponse attendancePolicy,
    // 참여자 관련
    List<ScheduleParticipantInfoResponse> participants
) {
    public record ScheduleLocationInfoResponse(
        Double latitude,
        Double longitude,
        String locationName
    ) {
    }

    public record ScheduleAttendancePolicyInfoResponse(
        // 아래 3개의 필드는 AttendancePolicy에서 그대로 가지고 올 것
        Long earlyCheckInMinutes,
        Long lateCheckInMinutes,
        Long lateToleranceMinutes,
        // Client에서 편하게 할 수 있도록 서버에서 위 3개의 값을 기반으로 연산해서 제공할 것
        Instant checkInStartAt,
        Instant onTimeEndAt,
        Instant lateEndAt
    ) {
    }

    public record ScheduleParticipantInfoResponse(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName,
        String profileImageUrl
    ) {
    }
}
