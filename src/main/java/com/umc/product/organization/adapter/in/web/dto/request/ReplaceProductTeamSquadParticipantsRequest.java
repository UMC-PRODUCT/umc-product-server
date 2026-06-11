package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamSquadParticipantsCommand;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

public record ReplaceProductTeamSquadParticipantsRequest(
    List<@Valid ProductTeamSquadParticipantRequest> participants
) {
    public ReplaceProductTeamSquadParticipantsCommand toCommand(Long squadId, Long requesterMemberId) {
        return ReplaceProductTeamSquadParticipantsCommand.of(
            squadId,
            requesterMemberId,
            Objects.requireNonNullElse(participants, List.<ProductTeamSquadParticipantRequest>of())
                .stream()
                .map(ProductTeamSquadParticipantRequest::toCommand)
                .toList()
        );
    }
}
