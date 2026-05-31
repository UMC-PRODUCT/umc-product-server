package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.application.port.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
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

    public static ScheduleInfo from(
        Schedule schedule,
        List<ScheduleParticipantDetailDto> participants,
        Long requesterMemberId
    ) {

        ScheduleBaseInfo baseInfo = ScheduleBaseInfo.from(schedule);

        // 요청자의 출석 상태 및 참여자 여부 확인
        AttendanceStatus myStatus = null;
        boolean isParticipant = false;

        for (ScheduleParticipantDetailDto p : participants) {
            if (p.memberId().equals(requesterMemberId)) {
                myStatus = p.attendanceStatus();
                isParticipant = true;
                break;
            }
        }

        return ScheduleInfo.builder()
            .baseInfo(baseInfo)
            .attendanceStatus(myStatus)
            .isParticipant(isParticipant)
            .participants(mapParticipants(participants))
            .build();
    }

    // ScheduleParticipantInfo 변환 헬퍼 메서드
    private static List<ScheduleParticipantInfo> mapParticipants(List<ScheduleParticipantDetailDto> participants) {
        return participants.stream()
            .map(p -> new ScheduleParticipantInfo(
                p.memberId(), p.name(), p.nickname(),
                p.schoolId(), p.schoolName(), p.profileImageUrl()
            )).toList();
    }
}
