package com.umc.product.analytics.adapter.in.web.dto.request;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import org.springframework.data.domain.Pageable;

public record AdminRiskChallengerRequest(
    Long gisuId,
    Long chapterId,
    Long schoolId,
    Integer riskThreshold
) {

    public AdminRiskChallengerQuery toQuery(Long requesterMemberId, Pageable pageable) {
        return AdminRiskChallengerQuery.of(
            requesterMemberId,
            gisuId,
            chapterId,
            schoolId,
            riskThreshold,
            pageable
        );
    }
}
