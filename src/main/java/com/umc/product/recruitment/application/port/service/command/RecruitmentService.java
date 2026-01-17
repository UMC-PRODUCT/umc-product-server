package com.umc.product.recruitment.application.port.service.command;

import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;
import org.springframework.stereotype.Service;

@Service
public class RecruitmentService implements CreateRecruitmentDraftFormResponseUseCase {

    @Override
    public CreateOrGetDraftFormResponseInfo createOrGet(CreateOrGetRecruitmentDraftCommand command) {
        return null;
    }
}
