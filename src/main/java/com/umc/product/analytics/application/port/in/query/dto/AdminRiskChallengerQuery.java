package com.umc.product.analytics.application.port.in.query.dto;

import org.springframework.data.domain.Pageable;

public record AdminRiskChallengerQuery(
    Long requesterMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    int riskThreshold,
    Pageable pageable
) {

    public static AdminRiskChallengerQuery of(
        Long requesterMemberId,
        Long gisuId,
        Long chapterId,
        Long schoolId,
        Integer riskThreshold,
        Pageable pageable
    ) {
        return new AdminRiskChallengerQuery(
            requesterMemberId,
            gisuId,
            chapterId,
            schoolId,
            riskThreshold != null ? riskThreshold : -8,
            pageable
        );
    }
}
