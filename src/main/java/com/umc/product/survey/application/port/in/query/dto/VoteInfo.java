package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.enums.FormOpenStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record VoteInfo(
    Long voteId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    FormOpenStatus status,
    Instant startsAt,
    Instant endsAtExclusive,
    LocalDate startDateKst,
    LocalDate endDateKst,  // inclusive endDate
    int totalParticipants,      // 집계용
    List<VoteOptionInfo> options,
    List<Long> mySelectedOptionIds // 미투표면 []
) {
    public record VoteOptionInfo(
        Long optionId,
        String content,
        int voteCount,
        BigDecimal voteRate
    ) {
    }
}
