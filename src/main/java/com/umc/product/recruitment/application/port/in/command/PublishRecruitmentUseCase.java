package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;

public interface PublishRecruitmentUseCase {
    void publish(PublishRecruitmentCommand command);
}
