package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.command.CreateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateLiveQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateLiveQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateLiveQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertMyInterviewEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentInterviewEvaluationService implements UpsertMyInterviewEvaluationUseCase,
        CreateLiveQuestionUseCase,
        UpdateLiveQuestionUseCase,
        DeleteLiveQuestionUseCase {

    @Override
    public GetMyInterviewEvaluationInfo upsert(UpsertMyInterviewEvaluationCommand command) {
        return null;
    }

    @Override
    public CreateLiveQuestionResult create(CreateLiveQuestionCommand command) {
        return null;
    }

    @Override
    public UpdateLiveQuestionResult update(UpdateLiveQuestionCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteLiveQuestionCommand command) {
    }
}
