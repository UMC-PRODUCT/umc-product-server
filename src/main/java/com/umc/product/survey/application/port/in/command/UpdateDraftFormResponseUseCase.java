package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.UpdateDraftFormResponseCommand;

public interface UpdateDraftFormResponseUseCase {
    void update(UpdateDraftFormResponseCommand command);
}
