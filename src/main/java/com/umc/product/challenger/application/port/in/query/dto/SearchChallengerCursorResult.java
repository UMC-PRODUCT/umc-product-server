package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerTrack;

public record SearchChallengerCursorResult(
    List<SearchChallengerItemInfo> content,
    Long nextCursor,
    boolean hasNext,
    Map<ChallengerPart, Long> partCounts,
    Map<ChallengerTrack, Long> trackCounts
) {
}
