package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.ScheduleAttendanceRequestCommand;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleParticipantAttendanceInfo;

public interface CreateScheduleParticipantUseCase {

    // 최초 요청 시 사용
    ScheduleParticipantAttendanceInfo createScheduleParticipantWithAttendance(ScheduleAttendanceRequestCommand command);
}
