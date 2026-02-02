package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.command.dto.CreateMyEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.MyEvaluationInfo;

public interface UpdateMyEvaluationUseCase {
    MyEvaluationInfo update(CreateMyEvaluationCommand command);
}
