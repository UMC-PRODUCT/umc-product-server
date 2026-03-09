package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;

public interface ManageGisuUseCase {

    Long create(CreateGisuCommand command);

    void deleteGisu(Long gisuId);

    void updateActiveGisu(Long gisuId);
}
