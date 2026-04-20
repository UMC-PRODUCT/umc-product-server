package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;

public record ScheduleInfo(

    ScheduleBaseInfo baseInfo,

    // 출석 관련
    // 해당 일정에 대한 요청자의 출석 상태입니다. 요청한 사람이 참여자가 아니거나 아직 출석 요청을 하지 않은 경우 null로 갑니다.
    AttendanceStatus attendanceStatus,

    // 참여자 관련
    boolean isParticipant, // 요청한 사용자가 해당 일정의 참여자인지 여부입니다. 참여자 목록에 요청한 사용자가 포함되어 있는지 여부와는 별개로, 서버 측에서 별도로 계산해서 제공합니다.
    List<ScheduleParticipantInfo> participants
) {
    public record ScheduleParticipantInfo(
        Long memberId,
        String name,
        String nickname,
        Long schoolId,
        String schoolName,
        String profileImageUrl
    ) {
    }
}
