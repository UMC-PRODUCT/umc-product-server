package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;

public interface ManageGisuUseCase {

    Long register(CreateGisuCommand command);

    void updateGisu(UpdateGisuCommand command);

    void deleteGisu(Long gisuId);

    void setCurrentGisu(Long gisuId);
}
