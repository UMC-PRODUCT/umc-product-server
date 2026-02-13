package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;

public interface CreateVoteUseCase {
    Long create(CreateVoteCommand createVoteCommand);
}
