package com.umc.product.analytics.application.service.query;

import com.umc.product.analytics.application.port.in.query.GetAdminDashboardActionQueueUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminDashboardContextUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminDashboardSummaryUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminOperationsOverviewUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminRiskChallengerUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminSchoolSummaryUseCase;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardContextInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import com.umc.product.analytics.application.port.out.LoadAdminDashboardAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminOperationsAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminRiskChallengerAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminSchoolAnalyticsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalyticsQueryService implements
    GetAdminDashboardSummaryUseCase,
    GetAdminDashboardActionQueueUseCase,
    GetAdminDashboardContextUseCase,
    GetAdminSchoolSummaryUseCase,
    GetAdminRiskChallengerUseCase,
    GetAdminOperationsOverviewUseCase {

    private final AdminAnalyticsScopeResolver scopeResolver;
    private final LoadAdminDashboardAnalyticsPort loadAdminDashboardAnalyticsPort;
    private final LoadAdminSchoolAnalyticsPort loadAdminSchoolAnalyticsPort;
    private final LoadAdminRiskChallengerAnalyticsPort loadAdminRiskChallengerAnalyticsPort;
    private final LoadAdminOperationsAnalyticsPort loadAdminOperationsAnalyticsPort;

    @Override
    public AdminDashboardSummaryInfo getSummary(AdminDashboardQuery query) {
        AdminAnalyticsScope scope = scopeResolver.resolve(
            query.requesterMemberId(),
            query.gisuId(),
            query.chapterId(),
            query.schoolId(),
            null
        );
        return loadAdminDashboardAnalyticsPort.getSummary(scope);
    }

    @Override
    public AdminDashboardActionQueueInfo getActionQueue(AdminDashboardActionQueueQuery query) {
        AdminAnalyticsScope scope = scopeResolver.resolve(query.requesterMemberId(), query.gisuId(), null, null, null);
        return loadAdminDashboardAnalyticsPort.getActionQueue(scope, query.riskThreshold());
    }

    @Override
    public AdminDashboardContextInfo getContext(Long requesterMemberId) {
        return AdminDashboardContextInfo.from(scopeResolver.resolve(requesterMemberId, null, null, null, null));
    }

    @Override
    public Page<AdminSchoolSummaryInfo> getSchoolSummaries(AdminSchoolSummaryQuery query) {
        AdminAnalyticsScope scope = scopeResolver.resolve(
            query.requesterMemberId(),
            query.gisuId(),
            query.chapterId(),
            null,
            null
        );
        return loadAdminSchoolAnalyticsPort.getSchoolSummaries(scope, query);
    }

    @Override
    public Page<AdminRiskChallengerInfo> getRiskChallengers(AdminRiskChallengerQuery query) {
        AdminAnalyticsScope scope = scopeResolver.resolve(
            query.requesterMemberId(),
            query.gisuId(),
            query.chapterId(),
            query.schoolId(),
            null
        );
        return loadAdminRiskChallengerAnalyticsPort.getRiskChallengers(scope, query);
    }

    @Override
    public AdminOperationsOverviewInfo getOperationsOverview(AdminOperationsOverviewQuery query) {
        AdminAnalyticsScope scope = scopeResolver.resolve(query.requesterMemberId(), query.gisuId(), null, null, null);
        return loadAdminOperationsAnalyticsPort.getOperationsOverview(scope, query);
    }
}
