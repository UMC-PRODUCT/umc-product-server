package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.MyEvaluationInfo;

public interface UpdateMyEvaluationUseCase {
    MyEvaluationInfo update(UpdateMyEvaluationCommand command);
}
