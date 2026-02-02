package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationType;
import com.umc.product.challenger.application.port.in.command.dto.DeactivateChallengerCommand;

public record DeactivateChallengerRequest(
        ChallengerDeactivationType deactivationType,
        Long modifiedBy,
        String reason
) {
    public DeactivateChallengerCommand toCommand(Long challengerId) {
        return new DeactivateChallengerCommand(
            challengerId,
            deactivationType,
            modifiedBy,
            reason);
    }
}
