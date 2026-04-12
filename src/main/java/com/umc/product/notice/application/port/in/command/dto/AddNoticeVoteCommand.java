package com.umc.product.notice.application.port.in.command.dto;

import java.time.Instant;
import java.util.List;

public record AddNoticeVoteCommand(
    Long createdMemberId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    Instant startsAt,
    Instant endsAtExclusive,
    List<String> options
) {
}
