package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionResult;

public interface UpdateLiveQuestionUseCase {
    UpdateLiveQuestionResult update(UpdateLiveQuestionCommand command);
}
