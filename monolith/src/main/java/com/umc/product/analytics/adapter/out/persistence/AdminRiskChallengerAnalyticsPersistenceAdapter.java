package com.umc.product.analytics.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import com.umc.product.analytics.application.port.out.LoadAdminRiskChallengerAnalyticsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminRiskChallengerAnalyticsPersistenceAdapter implements LoadAdminRiskChallengerAnalyticsPort {

    private final AdminRiskChallengerAnalyticsQueryRepository queryRepository;

    @Override
    public Page<AdminRiskChallengerInfo> getRiskChallengers(
        AdminAnalyticsScope scope,
        AdminRiskChallengerQuery query
    ) {
        return queryRepository.getRiskChallengers(scope, query);
    }
}
