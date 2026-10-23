package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerCursorResult(
    List<SearchChallengerItemInfo> content,
    Long nextCursor,
    boolean hasNext,
    Map<ChallengerPart, Long> partCounts
) {
}
