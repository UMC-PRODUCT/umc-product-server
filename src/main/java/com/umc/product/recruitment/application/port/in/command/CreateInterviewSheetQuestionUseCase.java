package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionResult;

public interface CreateInterviewSheetQuestionUseCase {
    CreateInterviewSheetQuestionResult create(CreateInterviewSheetQuestionCommand command);
}
