package com.umc.product.schedule.application.port.v2.in.query.dto;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;

// [운영진용] 일정 출석 현황 조회 dto
public record AdminScheduleInfo(

    ScheduleBaseInfo baseInfo,

    // 일정에 대한 참여자 리스트
    List<AdminScheduleParticipantInfo> participants
) {
    public record AdminScheduleParticipantInfo(
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
    }
}
