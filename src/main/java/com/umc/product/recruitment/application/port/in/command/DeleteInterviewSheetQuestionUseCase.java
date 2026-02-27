package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewSheetQuestionCommand;

public interface DeleteInterviewSheetQuestionUseCase {
    void delete(DeleteInterviewSheetQuestionCommand command);
}
