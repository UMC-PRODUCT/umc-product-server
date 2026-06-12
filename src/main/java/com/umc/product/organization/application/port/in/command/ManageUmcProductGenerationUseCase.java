package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductGenerationCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductGenerationCommand;

public interface ManageUmcProductGenerationUseCase {

    Long create(CreateUmcProductGenerationCommand command);

    void update(UpdateUmcProductGenerationCommand command);

    void delete(Long umcProductGenerationId, Long requesterMemberId);
}
