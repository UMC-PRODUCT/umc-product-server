package com.umc.product.analytics.adapter.out.persistence;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import com.umc.product.analytics.application.port.out.LoadAdminSchoolAnalyticsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSchoolAnalyticsPersistenceAdapter implements LoadAdminSchoolAnalyticsPort {

    private final AdminSchoolAnalyticsQueryRepository queryRepository;

    @Override
    public Page<AdminSchoolSummaryInfo> getSchoolSummaries(
        AdminAnalyticsScope scope,
        AdminSchoolSummaryQuery query
    ) {
        return queryRepository.getSchoolSummaries(scope, query);
    }
}
