package com.umc.product.test.application.port.in.command;

import java.util.List;

import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.in.command.dto.QaStepResult;

public interface SeedQaDataUseCase {
    List<QaStepResult> seedAll();

    QaStepResult seed(QaSeedStep step);
}
