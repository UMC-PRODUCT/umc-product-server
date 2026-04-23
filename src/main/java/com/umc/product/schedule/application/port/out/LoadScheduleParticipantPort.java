package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.application.port.out.dto.ScheduleParticipantDetailDto;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadScheduleParticipantPort {

    List<ScheduleParticipant> findAllByScheduleId(Long scheduleId);

    Optional<ScheduleParticipant> findByScheduleIdAndMemberId(Long scheduleId, Long requesterMemberId);

    // 특정 일정 ID 목록에 속한 모든 참여자의 상세 정보를 가져옵니다 (Member 테이블 조인 필요)
    List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIds(List<Long> scheduleIds);

    // 단일 일정에 대한 참여자 상세 정보를 조회합니다.
    List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleId(Long scheduleIds);

    List<ScheduleParticipantDetailDto> findParticipantDetailsByScheduleIdAndStatus(Long scheduleId,
                                                                                   AttendanceStatus attendanceStatus);

    // 일정의 참여자들의 memnerId를 조회합니다.
    Set<Long> findMemberIdsByScheduleId(Long scheduleId);
}
