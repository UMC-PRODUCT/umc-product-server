package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertMyInterviewEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;

public interface UpsertMyInterviewEvaluationUseCase {
    GetMyInterviewEvaluationInfo upsert(UpsertMyInterviewEvaluationCommand command);
}
