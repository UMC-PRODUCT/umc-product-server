package com.umc.product.challenger.application.port.in.query.dto;

import java.util.Map;

import org.springframework.data.domain.Page;

import com.umc.product.common.domain.enums.ChallengerPart;

public record SearchChallengerResult(
    Page<SearchChallengerItemInfo> page,
    Map<ChallengerPart, Long> partCounts
) {
}
