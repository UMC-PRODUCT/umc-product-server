package com.umc.product.schedule.application.port.v2.out;

import com.umc.product.schedule.domain.Schedule;

public interface SaveSchedulePort {

    Schedule save(Schedule schedule);
}
