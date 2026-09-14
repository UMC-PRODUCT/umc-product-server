package com.umc.product.schedule.application.port.out;

import java.util.List;

import com.umc.product.schedule.domain.ScheduleParticipant;

public interface SaveScheduleParticipantPort {

    ScheduleParticipant save(ScheduleParticipant scheduleParticipant);

    List<ScheduleParticipant> saveAll(List<ScheduleParticipant> participants);
}
