package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;

public interface UpdateRecruitmentDraftUseCase {
    RecruitmentDraftInfo update(UpdateRecruitmentDraftCommand command);
}
