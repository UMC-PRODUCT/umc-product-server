package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdatePublishedRecruitmentScheduleCommand;

public interface UpdatePublishedRecruitmentScheduleUseCase {
    RecruitmentPublishedInfo update(UpdatePublishedRecruitmentScheduleCommand command);
}
