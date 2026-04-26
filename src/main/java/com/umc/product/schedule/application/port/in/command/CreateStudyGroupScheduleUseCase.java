package com.umc.product.schedule.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupScheduleCommand;

public interface CreateStudyGroupScheduleUseCase {

    Long create(CreateStudyGroupScheduleCommand command);
}
