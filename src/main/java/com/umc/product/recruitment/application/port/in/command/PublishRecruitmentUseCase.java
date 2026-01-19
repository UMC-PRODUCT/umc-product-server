package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentInfo;

public interface PublishRecruitmentUseCase {
    PublishRecruitmentInfo publish(PublishRecruitmentCommand command);
}
