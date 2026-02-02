package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;

public record EditChallengerPartRequest(
        ChallengerPart newPart
) {
    public UpdateChallengerCommand toCommand(Long challengerId, Long modifiedBy) {
        return UpdateChallengerCommand.forPartChange(
                challengerId,
                this.newPart,
                modifiedBy
        );
    }
}
