package com.umc.product.analytics.adapter.in.web.dto.request;

import org.springframework.data.domain.Pageable;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;

public record AdminSchoolSummaryRequest(
    Long gisuId,
    Long chapterId,
    String search,
    Integer riskThreshold,
    String sort
) {

    public AdminSchoolSummaryQuery toQuery(Long requesterMemberId, Pageable pageable) {
        return AdminSchoolSummaryQuery.of(
            requesterMemberId,
            gisuId,
            chapterId,
            search,
            riskThreshold,
            pageable,
            sort
        );
    }
}
