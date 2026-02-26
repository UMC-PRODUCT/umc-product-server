package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
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
    public CreateVoteCommand toCreateVoteCommand() {
        return new CreateVoteCommand(
            createdMemberId,
            title,
            isAnonymous,
            allowMultipleChoice,
            startsAt,
            endsAtExclusive,
            options
        );
    }
}
