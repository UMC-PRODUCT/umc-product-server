package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminOperationsAnalyticsPort {

    AdminOperationsOverviewInfo getOperationsOverview(
        AdminAnalyticsScope scope,
        AdminOperationsOverviewQuery query
    );
}
