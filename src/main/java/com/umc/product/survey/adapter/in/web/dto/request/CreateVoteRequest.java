package com.umc.product.survey.adapter.in.web.dto.request;

import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record CreateVoteRequest(
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    LocalDate startDate,
    LocalDate endDate,
    List<String> options
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public CreateVoteCommand toCommand(Long createdMemberId) {
        Instant startsAt = (startDate == null) ? null : startDate.atStartOfDay(KST).toInstant();
        Instant endsAtExclusive = (endDate == null) ? null : endDate.plusDays(1).atStartOfDay(KST).toInstant();

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
