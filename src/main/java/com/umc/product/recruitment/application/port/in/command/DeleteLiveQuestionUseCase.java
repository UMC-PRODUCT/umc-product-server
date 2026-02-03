package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteLiveQuestionCommand;

public interface DeleteLiveQuestionUseCase {
    void delete(DeleteLiveQuestionCommand command);
}
