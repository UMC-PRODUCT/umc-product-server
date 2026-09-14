package com.umc.product.analytics.domain;

import com.umc.product.analytics.domain.AnalyticsDomainException;
import com.umc.product.analytics.domain.AnalyticsErrorCode;
import java.util.Set;

public enum AdminAnalyticsSort {
    RISK_CHALLENGER_COUNT_DESC("riskChallengerCount,desc"),
    ACTIVE_CHALLENGER_COUNT_DESC("activeChallengerCount,desc"),
    SCHOOL_NAME_ASC("schoolName,asc"),
    AVERAGE_POINT_SUM_ASC("averagePointSum,asc"),
    AVERAGE_POINT_SUM_DESC("averagePointSum,desc"),
    POINT_SUM_ASC("pointSum,asc"),
    POINT_SUM_DESC("pointSum,desc"),
    CREATED_AT_DESC("createdAt,desc");

    private static final Set<AdminAnalyticsSort> SCHOOL_SUMMARY_SORTS = Set.of(
        RISK_CHALLENGER_COUNT_DESC,
        ACTIVE_CHALLENGER_COUNT_DESC,
        SCHOOL_NAME_ASC,
        AVERAGE_POINT_SUM_ASC,
        AVERAGE_POINT_SUM_DESC
    );

    private final String value;

    AdminAnalyticsSort(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static AdminAnalyticsSort schoolSummaryOf(String value) {
        if (value == null || value.isBlank()) {
            return RISK_CHALLENGER_COUNT_DESC;
        }

        for (AdminAnalyticsSort sort : SCHOOL_SUMMARY_SORTS) {
            if (sort.value.equals(value)) {
                return sort;
            }
        }

        throw new AnalyticsDomainException(AnalyticsErrorCode.INVALID_SORT);
    }
}
