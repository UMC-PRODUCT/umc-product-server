package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.Map;
import org.springframework.data.domain.Page;

public record SearchChallengerResult(
        Page<SearchChallengerItemInfo> page,
        Map<ChallengerPart, Long> partCounts
) {
}
