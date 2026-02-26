package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionResult;

public interface CreateLiveQuestionUseCase {
    CreateLiveQuestionResult create(CreateLiveQuestionCommand command);
}
