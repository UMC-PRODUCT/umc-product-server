package com.umc.product.challenger.application.port.in.command.dto;

public record DeactivateChallengerCommand(
        Long challengerId,
        ChallengerDeactivationType deactivationType
) {
}
