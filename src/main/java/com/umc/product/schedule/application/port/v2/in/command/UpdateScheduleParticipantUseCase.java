package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.command.dto.result.ScheduleParticipantAttendanceResult;
import java.util.List;

public interface UpdateScheduleParticipantUseCase {

    List<ScheduleParticipantAttendanceResult> decideAttendances(List<DecideAttendanceCommand> commands);
}
