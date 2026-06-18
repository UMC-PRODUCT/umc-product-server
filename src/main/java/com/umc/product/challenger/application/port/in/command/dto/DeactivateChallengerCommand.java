package com.umc.product.challenger.application.port.in.command.dto;

public record DeactivateChallengerCommand(
        Long challengerId,
        ChallengerDeactivationType deactivationType,
        Long modifiedBy,
        String reason
) {
    public static DeactivateChallengerCommand of(
            Long challengerId,
            ChallengerDeactivationType deactivationType,
            Long modifiedBy,
            String reason
    ) {
        return new DeactivateChallengerCommand(challengerId, deactivationType, modifiedBy, reason);
    }
}
