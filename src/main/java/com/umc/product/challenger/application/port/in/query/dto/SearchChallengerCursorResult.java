package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Map;

public record SearchChallengerCursorResult(
        List<SearchChallengerItemInfo> content,
        Long nextCursor,
        boolean hasNext,
        Map<ChallengerPart, Long> partCounts
) {
}
