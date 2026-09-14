package com.umc.product.analytics.adapter.out.persistence;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.analytics.application.port.out.LoadAdminDashboardAnalyticsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDashboardAnalyticsPersistenceAdapter implements LoadAdminDashboardAnalyticsPort {

    private final AdminDashboardAnalyticsQueryRepository queryRepository;

    @Override
    public AdminDashboardSummaryInfo getSummary(AdminAnalyticsScope scope) {
        return queryRepository.getSummary(scope);
    }

    @Override
    public AdminDashboardActionQueueInfo getActionQueue(AdminAnalyticsScope scope, int riskThreshold) {
        return queryRepository.getActionQueue(scope, riskThreshold);
    }
}
