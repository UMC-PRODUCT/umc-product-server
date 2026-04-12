package com.umc.product.survey.adapter.in.web.dto.request;

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

}
