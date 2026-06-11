package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamFunctionalUnitCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamFunctionalUnitCommand;

public interface ManageProductTeamFunctionalUnitUseCase {

    Long create(CreateProductTeamFunctionalUnitCommand command);

    void update(UpdateProductTeamFunctionalUnitCommand command);

    void delete(Long functionalUnitId, Long requesterMemberId);
}
