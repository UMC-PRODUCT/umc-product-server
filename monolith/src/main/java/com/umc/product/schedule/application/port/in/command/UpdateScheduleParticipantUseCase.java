package com.umc.product.schedule.application.port.in.command;

import java.util.List;

import com.umc.product.schedule.application.port.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.result.ScheduleParticipantAttendanceResult;

public interface UpdateScheduleParticipantUseCase {

    List<ScheduleParticipantAttendanceResult> decideAttendances(List<DecideAttendanceCommand> commands);
}
