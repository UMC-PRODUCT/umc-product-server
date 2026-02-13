package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.SubmitVoteResponseCommand;

public interface SubmitVoteResponseUseCase {
    void submit(SubmitVoteResponseCommand command);
}
