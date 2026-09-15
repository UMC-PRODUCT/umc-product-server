package com.umc.product.analytics.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class AnalyticsDomainException extends BusinessException {

    public AnalyticsDomainException(AnalyticsErrorCode errorCode) {
        super(Domain.ANALYTICS, errorCode);
    }

    public AnalyticsDomainException(AnalyticsErrorCode errorCode, String message) {
        super(Domain.ANALYTICS, errorCode, message);
    }
}
