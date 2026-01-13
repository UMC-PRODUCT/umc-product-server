package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.SubmitFormResponseCommand;

public interface SubmitFormResponseUseCase {
    Long submit(SubmitFormResponseCommand command);
}
