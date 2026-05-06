package com.umc.product.project.application.port.in.command.dto;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.time.Instant;
import java.util.Objects;
import lombok.Builder;

@Builder
public record UpdateProjectMatchingRoundCommand(
    Long matchingRoundId,
    Long requesterMemberId,
    String name,
    String description,
    MatchingType type,
    MatchingPhase phase,
    Instant startsAt,
    Instant endsAt,
    Instant decisionDeadline
) {
    public UpdateProjectMatchingRoundCommand {
        Objects.requireNonNull(matchingRoundId, "matchingRoundId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
