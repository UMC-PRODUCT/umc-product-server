package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductSquadParticipantsCommand;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

public record ReplaceUmcProductSquadParticipantsRequest(
    List<@Valid UmcProductSquadParticipantRequest> participants
) {
    public ReplaceUmcProductSquadParticipantsCommand toCommand(Long squadId, Long requesterMemberId) {
        return ReplaceUmcProductSquadParticipantsCommand.of(
            squadId,
            requesterMemberId,
            Objects.requireNonNullElse(participants, List.<UmcProductSquadParticipantRequest>of())
                .stream()
                .map(UmcProductSquadParticipantRequest::toCommand)
                .toList()
        );
    }
}
