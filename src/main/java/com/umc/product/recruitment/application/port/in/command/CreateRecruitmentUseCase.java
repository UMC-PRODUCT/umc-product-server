package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;

public interface CreateRecruitmentUseCase {
    Long create(CreateRecruitmentCommand command);
}
