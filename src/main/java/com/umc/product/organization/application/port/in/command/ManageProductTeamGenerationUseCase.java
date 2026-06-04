package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamGenerationCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamGenerationCommand;

public interface ManageProductTeamGenerationUseCase {

    Long create(CreateProductTeamGenerationCommand command);

    void update(UpdateProductTeamGenerationCommand command);

    void delete(Long productTeamGenerationId, Long requesterMemberId);
}
