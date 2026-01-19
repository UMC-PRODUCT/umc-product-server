package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;

public interface CreateScheduleUseCase {
    
    Long create(CreateScheduleCommand command);
}
