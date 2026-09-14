package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminDashboardAnalyticsPort {

    AdminDashboardSummaryInfo getSummary(AdminAnalyticsScope scope);

    AdminDashboardActionQueueInfo getActionQueue(AdminAnalyticsScope scope, int riskThreshold);
}
