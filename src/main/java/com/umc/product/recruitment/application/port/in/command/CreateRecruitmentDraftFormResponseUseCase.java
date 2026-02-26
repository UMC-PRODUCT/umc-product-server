package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseInfo;

public interface CreateRecruitmentDraftFormResponseUseCase {
    CreateDraftFormResponseInfo create(CreateDraftFormResponseCommand command);
}
