package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;

public interface SubmitRecruitmentApplicationUseCase {
    SubmitRecruitmentApplicationInfo submit(SubmitRecruitmentApplicationCommand command);
}
