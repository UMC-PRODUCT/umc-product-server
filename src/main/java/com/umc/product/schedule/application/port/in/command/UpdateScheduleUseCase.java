package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleLocationInfo;

public interface UpdateScheduleUseCase {

    void update(UpdateScheduleCommand command);

    UpdateScheduleLocationInfo updateLocation(UpdateScheduleLocationCommand command);
}
