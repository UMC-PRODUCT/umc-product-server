package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;

/**
 * 일정에 관련된 사항을 변경할 때 사용하는 DTO 입니다.
 * <p>
 * 제공되지 않은 필드는 변경하지 않는 것으로 간주합니다.
 * <p>
 * 필드에 대한 자세한 사항은 {@link CreateScheduleRequest}을 참고해주시면 됩니다.
 */
@Builder
public record EditScheduleRequest(
    String name,
    String description,
    Set<ScheduleTag> tags,
    Instant startsAt,
    Instant endsAt,
    // 하루종일 일정은 따로 서버측에서 저장하지 않고,
    // 클라이언트 단에서 KST 기준 Instant로 알아서 변환하도록 합니다.
    ScheduleLocationRequest location,
    ScheduleAttendancePolicyRequest attendancePolicy,
    // 참여자는 중복되지 않도록 Set으로 받습니다.
    // 참여자 목록에는 반드시 요청한 사용자가 포함되어 있어야 하며,
    // 서버 측 생성자에서 add()를 통해서 강제로 참여시켜야 합니다.
    // (기획단 변경이 있기 전까지는 해당 사항을 유지합니다.)
    Set<Long> participantMemberIds
) {
    // 정팩메 적극적으로 활용해주세요
}
