package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationType;
import com.umc.product.challenger.application.port.in.command.dto.DeactivateChallengerCommand;

public record DeactivateChallengerRequest(
        ChallengerDeactivationType deactivationType
) {
    public DeactivateChallengerCommand toCommand(Long challengerId) {
        return new DeactivateChallengerCommand(challengerId, deactivationType);
    }
}
