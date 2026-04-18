package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.v2.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.domain.ScheduleParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleParticipantPersistenceAdapter implements
    SaveScheduleParticipantPort {

    private final ScheduleParticipantJpaRepository scheduleParticipantJpaRepository;

    @Override
    public ScheduleParticipant save(ScheduleParticipant scheduleParticipant) {
        return scheduleParticipantJpaRepository.save(scheduleParticipant);
    }
}
