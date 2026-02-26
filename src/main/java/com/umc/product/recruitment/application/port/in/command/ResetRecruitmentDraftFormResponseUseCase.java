package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.ResetDraftFormResponseCommand;

public interface ResetRecruitmentDraftFormResponseUseCase {
    CreateDraftFormResponseInfo reset(ResetDraftFormResponseCommand command);
}
