package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record ReplaceProductTeamMemberFunctionalMembershipsCommand(
    Long productTeamMemberId,
    Long requesterMemberId,
    List<ProductTeamFunctionalMembershipCommand> functionalMemberships
) {
    public static ReplaceProductTeamMemberFunctionalMembershipsCommand of(
        Long productTeamMemberId,
        Long requesterMemberId,
        List<ProductTeamFunctionalMembershipCommand> functionalMemberships
    ) {
        return new ReplaceProductTeamMemberFunctionalMembershipsCommand(
            productTeamMemberId,
            requesterMemberId,
            functionalMemberships
        );
    }
}
