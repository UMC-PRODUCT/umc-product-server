package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;

public interface UpsertRecruitmentFormQuestionsUseCase {
    RecruitmentApplicationFormInfo upsert(UpsertRecruitmentFormQuestionsCommand command);
}
