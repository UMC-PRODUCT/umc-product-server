package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.DeleteFormResponseCommand;

public interface DeleteFormResponseUseCase {
    void delete(DeleteFormResponseCommand command);
}
