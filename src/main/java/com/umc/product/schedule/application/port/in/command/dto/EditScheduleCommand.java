package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.adapter.in.web.v2.dto.request.ScheduleAttendancePolicyRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.ScheduleLocationRequest;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

@Builder
public record EditScheduleCommand(
    Long scheduleId,
    String name,
    String description,
    Set<ScheduleTag> tags,
    Instant startsAt,
    Instant endsAt,
    ScheduleLocationRequest location,
    // patch 요청에서 대면 일정의 위치를 유지하거나, 대면 일정을 비대면 일정을 바꾸는 경우를 고려햐여, 명시적 플래그 필드를 추가합니다.
    // null : 대면 유지 or 비대면 유지
    // true : 비대면으로 변경
    // false : 대면으로 변경
    Boolean isOnline, // 대면/비대면 구분
    ScheduleAttendancePolicyRequest attendancePolicy,
    Set<Long> participantMemberIds
) {

    // 참여자가 변경이 요청되었는지 확인, 즉 request의 participantMemberIds가 null이 아닌지 확인
    // 실제로 변경되었는지는 service 단에서 판별합니다.
    public boolean isParticipantsUpdateRequested() {
        return participantMemberIds != null;
    }

    // 대면 변경 여부 확인
    public boolean isChangingToOffline() {
        return Boolean.FALSE.equals(isOnline);
    }

    // 비대면 변경 여부 확인
    public boolean isChangingToOnline() {
        return Boolean.TRUE.equals(isOnline);
    }

    // 검증
    public void validate() {
        // 대면으로 변경하면서 location이 없으면 에러
        if (isChangingToOffline() && location == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.OFFLINE_SCHEDULE_REQUIRES_LOCATION);
        }

        // 비대면으로 변경하면서 location이 있으면 에러
        if (isChangingToOnline() && location != null) {
            throw new ScheduleDomainException(ScheduleErrorCode.ONLINE_SCHEDULE_SHOULD_NOT_HAVE_LOCATION);
        }

    }
}
