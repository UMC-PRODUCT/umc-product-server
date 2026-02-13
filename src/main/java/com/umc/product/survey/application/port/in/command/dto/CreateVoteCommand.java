package com.umc.product.survey.application.port.in.command.dto;

import java.time.Instant;
import java.util.List;

public record CreateVoteCommand(
    Long createdMemberId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    Instant startsAt,
    Instant endsAtExclusive,
    List<String> options
) {
}
