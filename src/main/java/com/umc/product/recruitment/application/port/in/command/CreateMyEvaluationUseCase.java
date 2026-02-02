package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateMyEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.dto.MyEvaluationInfo;

public interface CreateMyEvaluationUseCase {
    MyEvaluationInfo create(CreateMyEvaluationCommand command);
}
