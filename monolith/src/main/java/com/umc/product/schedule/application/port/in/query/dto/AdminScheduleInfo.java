package com.umc.product.schedule.application.port.in.query.dto;

import com.umc.product.schedule.application.port.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
// [운영진용] 일정 출석 현황 조회 dto
public record AdminScheduleInfo(

    ScheduleBaseInfo baseInfo,

    // 일정에 대한 참여자 리스트
    List<AdminScheduleParticipantInfo> participants
) {

    public static AdminScheduleInfo from(Schedule schedule,
                                         List<ScheduleParticipantDetailDto> participants) {

        ScheduleBaseInfo baseInfo = ScheduleBaseInfo.from(schedule);

        return AdminScheduleInfo.builder()
            .baseInfo(baseInfo)
            .participants(mapAdminParticipants(participants))
            .build();
    }

    // AdminScheduleParticipantInfo 변환 헬퍼 메서드
    private static List<AdminScheduleParticipantInfo> mapAdminParticipants(
        List<ScheduleParticipantDetailDto> participants) {
        return participants.stream()
            .map(p -> AdminScheduleParticipantInfo.builder()
                .memberId(p.memberId())
                .name(p.name())
                .nickname(p.nickname())
                .schoolId(p.schoolId())
                .schoolName(p.schoolName())
                .profileImageUrl(p.profileImageUrl())
                .attendanceStatus(p.attendanceStatus())
                .isLocationVerified(p.isLocationVerified())
                .excuseReason(p.excuseReason())
                .build()
            ).toList();
    }

    @Builder(access = AccessLevel.PRIVATE)
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
