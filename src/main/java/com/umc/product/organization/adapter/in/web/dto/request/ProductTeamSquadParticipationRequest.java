package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ProductTeamSquadParticipationCommand;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductTeamSquadParticipationRequest(
    @NotNull Long squadId,
    @NotNull ProductTeamSquadRole role,
    @NotNull ProductTeamPosition position,
    @Size(max = 200) String responsibilityTitle,
    @Size(max = 1000) String responsibilityDescription
) {
    public ProductTeamSquadParticipationCommand toCommand() {
        return ProductTeamSquadParticipationCommand.of(
            squadId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
