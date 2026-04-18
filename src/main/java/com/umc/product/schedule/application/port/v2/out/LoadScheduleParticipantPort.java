package com.umc.product.schedule.application.port.v2.out;

import com.umc.product.schedule.domain.ScheduleParticipant;
import java.util.List;

public interface LoadScheduleParticipantPort {

    List<ScheduleParticipant> findAllByScheduleId(Long ScheduleId);
}
