package com.umc.product.maintenance.application.port.in.command;

import com.umc.product.maintenance.application.port.in.command.dto.StartMaintenanceCommand;

public interface ManageMaintenanceUseCase {

    Long start(StartMaintenanceCommand command);

    void forceEnd(Long windowId, Long requestedBy);
}
