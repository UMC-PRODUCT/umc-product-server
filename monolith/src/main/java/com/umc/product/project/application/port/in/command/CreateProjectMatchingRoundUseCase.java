package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;

public interface CreateProjectMatchingRoundUseCase {

    Long create(CreateProjectMatchingRoundCommand command);
}
