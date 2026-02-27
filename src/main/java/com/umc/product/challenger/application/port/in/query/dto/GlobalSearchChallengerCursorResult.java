package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;

public record GlobalSearchChallengerCursorResult(
        List<GlobalSearchChallengerItemInfo> content,
        Long nextCursor,
        boolean hasNext
) {
}
