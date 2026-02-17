package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;

public interface CreateRecruitmentUseCase {
    CreateRecruitmentInfo create(CreateRecruitmentCommand command);

    CreateRecruitmentInfo createExtension(CreateExtensionCommand command);
}
