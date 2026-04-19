package com.umc.product.schedule.application.port.v2.out.dto;

import com.umc.product.schedule.domain.enums.AttendanceStatus;

public record ScheduleParticipantDetailDto(
    // 1. 매핑 및 필터링을 위한 키 값
    Long scheduleId, // 일정별로 그룹화(groupingBy) 하기 위해 필수!
    Long memberId,   // 내 출석 상태를 찾기 위해 필수!

    // 2. 참여자 정보 (Member 조인 필요)
    String name,
    String nickname,
    Long schoolId,
    String schoolName,
    String profileImageUrl,

    // 3. 출석 상태 (ScheduleParticipant 내장 객체에서 가져옴)
    AttendanceStatus attendanceStatus,
    String excuseReason // 사유가 없으면 null
) {
}
