package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerPointCommand;

public record EditChallengerPointRequest(
        String newDescription
) {
    public UpdateChallengerPointCommand toCommand(Long challengerPointId) {
        return new UpdateChallengerPointCommand(challengerPointId, newDescription);
    }
}
