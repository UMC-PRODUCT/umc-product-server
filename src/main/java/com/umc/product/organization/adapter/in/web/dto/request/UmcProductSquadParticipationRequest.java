package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UmcProductSquadParticipationCommand;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UmcProductSquadParticipationRequest(
    @NotNull Long squadId,
    @NotNull UmcProductSquadRole role,
    @NotNull UmcProductPosition position,
    @Size(max = 200) String responsibilityTitle,
    @Size(max = 1000) String responsibilityDescription
) {
    public UmcProductSquadParticipationCommand toCommand() {
        return UmcProductSquadParticipationCommand.of(
            squadId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
