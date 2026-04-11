package com.umc.product.schedule.adapter.in.web.v2.dto.request;

import java.time.Instant;

public record ScheduleAttendancePolicyRequest(
    Instant checkInStartAt, // 출석 요청 시작 가능 시점
    Instant onTimeEndAt, // 출석으로 인정하는 마감 시간
    Instant lateEndAt // 지각으로 인정하는 마감 시간
) {
    // 클라이언트에게 아래와 같은 개념을 설명하는 것 보다, 시간을 직관적으로 입력받아서 제공하는게 빠를 듯 함.
    // 객체 create 시에 validation을 반드시 거쳐야 함.

    // 검증해야 하는 내용: checkInStartAt < startAt < onTimeEndAt < lateEndAt < endsAt

    // earlyCheckInMinutes, lateCheckInMinutes, lateToleranceMinutes으로
    // 변환하는 정팩메를 생성할 것
}
