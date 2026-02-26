package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionResult;

public interface UpdateInterviewSheetQuestionUseCase {
    UpdateInterviewSheetQuestionResult update(UpdateInterviewSheetQuestionCommand command);
}
