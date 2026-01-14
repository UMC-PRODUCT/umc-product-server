package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;

public interface DeleteRecruitmentUseCase {
    void delete(DeleteRecruitmentCommand command);
}
