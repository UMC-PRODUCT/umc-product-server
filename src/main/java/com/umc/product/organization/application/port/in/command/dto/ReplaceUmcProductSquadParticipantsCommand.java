package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record ReplaceUmcProductSquadParticipantsCommand(
    Long squadId,
    Long requesterMemberId,
    List<UmcProductSquadParticipantCommand> participants
) {
    public static ReplaceUmcProductSquadParticipantsCommand of(
        Long squadId,
        Long requesterMemberId,
        List<UmcProductSquadParticipantCommand> participants
    ) {
        return new ReplaceUmcProductSquadParticipantsCommand(squadId, requesterMemberId, participants);
    }
}
