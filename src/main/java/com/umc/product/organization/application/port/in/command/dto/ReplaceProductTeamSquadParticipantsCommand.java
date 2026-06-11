package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record ReplaceProductTeamSquadParticipantsCommand(
    Long squadId,
    Long requesterMemberId,
    List<ProductTeamSquadParticipantCommand> participants
) {
    public static ReplaceProductTeamSquadParticipantsCommand of(
        Long squadId,
        Long requesterMemberId,
        List<ProductTeamSquadParticipantCommand> participants
    ) {
        return new ReplaceProductTeamSquadParticipantsCommand(squadId, requesterMemberId, participants);
    }
}
