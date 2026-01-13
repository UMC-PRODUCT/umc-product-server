package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormResponseCommand;

public interface CreateDraftFormResponseUseCase {
    Long create(CreateDraftFormResponseCommand command);
}
