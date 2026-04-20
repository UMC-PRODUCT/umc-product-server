package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.EditScheduleCommand;

public interface UpdateScheduleUseCase {

    Long update(EditScheduleCommand command);
}
