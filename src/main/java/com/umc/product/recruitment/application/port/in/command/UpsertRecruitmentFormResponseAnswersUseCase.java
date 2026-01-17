package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;

public interface UpsertRecruitmentFormResponseAnswersUseCase {
    UpsertRecruitmentFormResponseAnswersInfo upsert(UpsertRecruitmentFormResponseAnswersCommand command);
}
