package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentResult;

public interface CreateInterviewAssignmentUseCase {
    CreateInterviewAssignmentResult create(CreateInterviewAssignmentCommand command);
}
