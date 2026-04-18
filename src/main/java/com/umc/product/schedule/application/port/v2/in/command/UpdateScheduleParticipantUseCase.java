package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleParticipantAttendanceInfo;
import java.util.List;

public interface UpdateScheduleParticipantUseCase {

    List<ScheduleParticipantAttendanceInfo> decideAttendances(List<DecideAttendanceCommand> commands);
}
