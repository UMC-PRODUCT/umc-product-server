package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.CreateScheduleCommand;

public interface CreateScheduleUseCase {

    Long create(CreateScheduleCommand command);
}
