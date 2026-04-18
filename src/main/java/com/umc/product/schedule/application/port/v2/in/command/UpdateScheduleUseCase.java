package com.umc.product.schedule.application.port.v2.in.command;

import com.umc.product.schedule.application.port.v2.in.command.dto.EditScheduleCommand;

public interface UpdateScheduleUseCase {

    Long update(EditScheduleCommand command);
}
