package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.Set;

/**
 * 일정 생성을 위한 Request DTO 입니다.
 *
 * @param name                 일정명
 * @param description          일정에 대한 설명
 * @param tags                 일정에 관련된 태그
 * @param startsAt             일정 시작 시간
 * @param endsAt               일정 종료 시간
 * @param location             일정 위치 (비대면인 경우, null)
 * @param attendancePolicy     출석/지각/결석과 관련된 정책입니다. 제공되지 않은 경우 출석을 요하지 않는 일정으로 간주합니다.
 * @param participantMemberIds 일정에 참여하는 사용자의 memberId의 배열입니다. 요청한 사용자는 자동으로 참여하도록 설정되며, 중복값은 자동으로 필터링 됩니다.
 */
public record CreateScheduleRequest(
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
    // toEntity() 정팩메 만들어주세요!
}
