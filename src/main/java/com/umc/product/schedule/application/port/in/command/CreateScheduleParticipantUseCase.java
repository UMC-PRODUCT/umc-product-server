package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.ExcuseScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.ScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.result.ScheduleParticipantAttendanceResult;

public interface CreateScheduleParticipantUseCase {

    // 출석 요청 시 사용, 사유 제출 X
    ScheduleParticipantAttendanceResult createScheduleParticipantWithAttendance(ScheduleAttendanceCommand command);

    // 사유 제출 시 사용
    ScheduleParticipantAttendanceResult createExcusedScheduleParticipantWithAttendance(
        ExcuseScheduleAttendanceCommand command
    );
}
