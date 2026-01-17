package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;

public interface DeleteRecruitmentFormResponseUseCase {
    void delete(DeleteRecruitmentFormResponseCommand command);
}
