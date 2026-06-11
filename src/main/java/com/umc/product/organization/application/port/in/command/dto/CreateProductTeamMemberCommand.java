package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record CreateProductTeamMemberCommand(
    Long requesterMemberId,
    Long memberId,
    String introduction,
    String profileImageId,
    List<ProductTeamFunctionalMembershipCommand> functionalMemberships,
    List<ProductTeamSquadParticipationCommand> squadParticipations
) {
    public static CreateProductTeamMemberCommand of(
        Long requesterMemberId,
        Long memberId,
        String introduction,
        String profileImageId,
        List<ProductTeamFunctionalMembershipCommand> functionalMemberships,
        List<ProductTeamSquadParticipationCommand> squadParticipations
    ) {
        return new CreateProductTeamMemberCommand(
            requesterMemberId,
            memberId,
            introduction,
            profileImageId,
            functionalMemberships,
            squadParticipations
        );
    }
}
