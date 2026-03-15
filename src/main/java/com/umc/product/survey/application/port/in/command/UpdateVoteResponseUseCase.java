package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.UpdateVoteResponseCommand;

public interface UpdateVoteResponseUseCase {
    void update(UpdateVoteResponseCommand command);
}
