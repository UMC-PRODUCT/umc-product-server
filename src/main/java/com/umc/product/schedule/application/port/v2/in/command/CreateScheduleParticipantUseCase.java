package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.ExcuseScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.command.dto.ScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleParticipantAttendanceInfo;

public interface CreateScheduleParticipantUseCase {

    // 최초 요청 시 사용, 사유 제출 X
    ScheduleParticipantAttendanceInfo createScheduleParticipantWithAttendance(ScheduleAttendanceCommand command);

    // 사유 제출 시 사용
    ScheduleParticipantAttendanceInfo createExcusedScheduleParticipantWithAttendance(
        ExcuseScheduleAttendanceCommand command
    );
}
