package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.UpdateProjectMatchingRoundCommand;

public interface UpdateProjectMatchingRoundUseCase {

    void update(UpdateProjectMatchingRoundCommand command);
}
