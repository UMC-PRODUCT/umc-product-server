package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.DeleteVoteCommand;

public interface DeleteVoteUseCase {
    void delete(DeleteVoteCommand command);
}
