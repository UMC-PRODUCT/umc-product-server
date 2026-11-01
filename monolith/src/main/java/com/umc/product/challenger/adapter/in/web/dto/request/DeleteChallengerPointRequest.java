package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerPointCommand;

public record DeleteChallengerPointRequest(
) {
    public DeleteChallengerPointCommand toCommand(Long challengerPointId) {
        return new DeleteChallengerPointCommand(challengerPointId);
    }
}
