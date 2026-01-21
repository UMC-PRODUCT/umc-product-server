package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;

public interface UpdateScheduleUseCase {

    void update(UpdateScheduleCommand command);
}
