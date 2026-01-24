package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormQuestionCommand;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;

public interface DeleteRecruitmentFormQuestionUseCase {
    RecruitmentApplicationFormInfo delete(DeleteRecruitmentFormQuestionCommand command);
}
