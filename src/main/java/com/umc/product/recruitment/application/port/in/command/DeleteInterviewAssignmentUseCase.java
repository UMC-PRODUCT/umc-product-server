package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.query.dto.DeleteInterviewAssignmentResult;

public interface DeleteInterviewAssignmentUseCase {
    DeleteInterviewAssignmentResult delete(DeleteInterviewAssignmentCommand command);
}
