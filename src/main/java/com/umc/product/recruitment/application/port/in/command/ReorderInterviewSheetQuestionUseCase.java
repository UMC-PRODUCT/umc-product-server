package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionResult;

public interface ReorderInterviewSheetQuestionUseCase {
    ReorderInterviewSheetQuestionResult reorder(ReorderInterviewSheetQuestionCommand command);
}
