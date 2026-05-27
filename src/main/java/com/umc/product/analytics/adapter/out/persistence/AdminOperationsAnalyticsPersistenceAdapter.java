package com.umc.product.analytics.adapter.out.persistence;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsInfo;
import com.umc.product.analytics.application.port.out.LoadAdminOperationsAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminOperationsSchoolsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminOperationsAnalyticsPersistenceAdapter implements
    LoadAdminOperationsAnalyticsPort,
    LoadAdminOperationsSchoolsPort {

    private final AdminOperationsAnalyticsQueryRepository queryRepository;

    @Override
    public AdminOperationsOverviewInfo getOperationsOverview(
        AdminAnalyticsScope scope,
        AdminOperationsOverviewQuery query
    ) {
        return queryRepository.getOperationsOverview(scope, query);
    }

    @Override
    public AdminOperationsSchoolsInfo getOperationsSchools(AdminAnalyticsScope scope) {
        return queryRepository.getOperationsSchools(scope);
    }
}
