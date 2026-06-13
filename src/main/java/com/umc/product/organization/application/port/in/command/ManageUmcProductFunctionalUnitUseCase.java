package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductFunctionalUnitCommand;

public interface ManageUmcProductFunctionalUnitUseCase {

    Long create(CreateUmcProductFunctionalUnitCommand command);

    void update(UpdateUmcProductFunctionalUnitCommand command);

    void delete(Long functionalUnitId, Long requesterMemberId);
}
