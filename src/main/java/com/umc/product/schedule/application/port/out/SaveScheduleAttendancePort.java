package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.ScheduleAttendance;

public interface SaveScheduleAttendancePort {

    ScheduleAttendance save(ScheduleAttendance attendance);
}
