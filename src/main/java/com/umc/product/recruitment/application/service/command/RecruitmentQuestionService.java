package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.CreateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.ReorderInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentQuestionService implements CreateInterviewSheetQuestionUseCase,
        UpdateInterviewSheetQuestionUseCase,
        DeleteInterviewSheetQuestionUseCase,
        ReorderInterviewSheetQuestionUseCase {

    @Override
    public CreateInterviewSheetQuestionResult create(CreateInterviewSheetQuestionCommand command) {
        return null;
    }

    @Override
    public UpdateInterviewSheetQuestionResult update(UpdateInterviewSheetQuestionCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteInterviewSheetQuestionCommand command) {

    }

    @Override
    public ReorderInterviewSheetQuestionResult reorder(ReorderInterviewSheetQuestionCommand command) {
        return null;
    }
}
