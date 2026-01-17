package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;

public interface CreateRecruitmentDraftFormResponseUseCase {
    CreateOrGetDraftFormResponseInfo createOrGet(CreateOrGetRecruitmentDraftCommand command);
}
