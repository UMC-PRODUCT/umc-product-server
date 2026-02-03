package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;

public interface UpdateFinalStatusUseCase {
    UpdateFinalStatusResult update(UpdateFinalStatusCommand command);
}
