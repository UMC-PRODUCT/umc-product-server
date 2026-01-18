package com.umc.product.recruitment.application.port.service.command;

import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import org.springframework.stereotype.Service;

@Service
public class RecruitmentService implements CreateRecruitmentDraftFormResponseUseCase,
        UpsertRecruitmentFormResponseAnswersUseCase,
        DeleteRecruitmentFormResponseUseCase,
        SubmitRecruitmentApplicationUseCase,
        CreateRecruitmentUseCase {

    @Override
    public CreateOrGetDraftFormResponseInfo createOrGet(CreateOrGetRecruitmentDraftCommand command) {
        return null;
    }

    @Override
    public UpsertRecruitmentFormResponseAnswersInfo upsert(
            UpsertRecruitmentFormResponseAnswersCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteRecruitmentFormResponseCommand command) {
    }

    @Override
    public SubmitRecruitmentApplicationInfo submit(SubmitRecruitmentApplicationCommand command) {
        return null;
    }

    @Override
    public Long create(CreateRecruitmentCommand command) {
        return null;
    }
}
