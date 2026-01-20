package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;

public record EditChallengerRequest(
        ChallengerPart newPart
) {
    public UpdateChallengerCommand toCommand(Long challengerId) {
        return new UpdateChallengerCommand(challengerId, newPart);
    }
}
