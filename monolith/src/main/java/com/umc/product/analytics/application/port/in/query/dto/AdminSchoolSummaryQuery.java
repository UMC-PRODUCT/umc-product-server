package com.umc.product.analytics.application.port.in.query.dto;

import com.umc.product.analytics.domain.AdminAnalyticsSort;
import org.springframework.data.domain.Pageable;

public record AdminSchoolSummaryQuery(
    Long requesterMemberId,
    Long gisuId,
    Long chapterId,
    String search,
    int riskThreshold,
    Pageable pageable,
    AdminAnalyticsSort sort
) {

    public static AdminSchoolSummaryQuery of(
        Long requesterMemberId,
        Long gisuId,
        Long chapterId,
        String search,
        Integer riskThreshold,
        Pageable pageable,
        String sort
    ) {
        return new AdminSchoolSummaryQuery(
            requesterMemberId,
            gisuId,
            chapterId,
            search,
            riskThreshold != null ? riskThreshold : -8,
            pageable,
            AdminAnalyticsSort.schoolSummaryOf(sort)
        );
    }
}
