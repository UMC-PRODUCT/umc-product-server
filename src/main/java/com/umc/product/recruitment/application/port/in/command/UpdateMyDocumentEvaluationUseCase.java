package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyDocumentEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;

public interface UpdateMyDocumentEvaluationUseCase {
    GetMyDocumentEvaluationInfo update(UpdateMyDocumentEvaluationCommand command);
}
