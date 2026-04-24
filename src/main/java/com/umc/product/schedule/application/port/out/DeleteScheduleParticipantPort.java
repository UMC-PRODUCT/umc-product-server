package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.ScheduleParticipant;
import java.util.List;

public interface DeleteScheduleParticipantPort {

    void deleteAll(List<ScheduleParticipant> participants);
}
