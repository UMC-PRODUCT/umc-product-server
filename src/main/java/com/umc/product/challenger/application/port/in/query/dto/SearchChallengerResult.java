package com.umc.product.challenger.application.port.in.query.dto;

import java.util.Map;

import org.springframework.data.domain.Page;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerTrack;

public record SearchChallengerResult(
    Page<SearchChallengerItemInfo> page,
    Map<ChallengerPart, Long> partCounts,
    Map<ChallengerTrack, Long> trackCounts
) {
}
