package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ProductTeamSquadParticipantCommand;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductTeamSquadParticipantRequest(
    @NotNull Long productTeamMemberId,
    @NotNull ProductTeamSquadRole role,
    @NotNull ProductTeamPosition position,
    @Size(max = 200) String responsibilityTitle,
    @Size(max = 1000) String responsibilityDescription
) {
    public ProductTeamSquadParticipantCommand toCommand() {
        return ProductTeamSquadParticipantCommand.of(
            productTeamMemberId,
            role,
            position,
            responsibilityTitle,
            responsibilityDescription
        );
    }
}
