package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentQuestionOptionCommand;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;

public interface DeleteRecruitmentQuestionOptionUseCase {
    RecruitmentApplicationFormInfo delete(DeleteRecruitmentQuestionOptionCommand command);
}
