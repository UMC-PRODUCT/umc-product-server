package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.ScheduleParticipant;
import java.util.List;

public interface DeleteScheduleParticipantPort {

    void deleteAll(List<ScheduleParticipant> participants);

    /**
     * 특정 일정에 속한 모든 ScheduleParticipant를 단일 벌크 쿼리로 삭제합니다.
     */
    void deleteByScheduleId(Long scheduleId);
}
