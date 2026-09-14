package com.umc.product.analytics.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;
import com.umc.product.analytics.application.port.out.LoadAdminDashboardAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminOperationsAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminRiskChallengerAnalyticsPort;
import com.umc.product.analytics.application.port.out.LoadAdminSchoolAnalyticsPort;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import com.umc.product.analytics.domain.AdminAnalyticsScopeType;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAnalyticsQueryService")
class AdminAnalyticsQueryServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long GISU_ID = 7L;

    @Mock
    AdminAnalyticsScopeResolver scopeResolver;

    @Mock
    LoadAdminDashboardAnalyticsPort loadAdminDashboardAnalyticsPort;

    @Mock
    LoadAdminSchoolAnalyticsPort loadAdminSchoolAnalyticsPort;

    @Mock
    LoadAdminRiskChallengerAnalyticsPort loadAdminRiskChallengerAnalyticsPort;

    @Mock
    LoadAdminOperationsAnalyticsPort loadAdminOperationsAnalyticsPort;

    @InjectMocks
    AdminAnalyticsQueryService sut;

    @Test
    @DisplayName("summary 중앙 운영진은 전체 스코프의 KPI를 조회한다")
    void summary_중앙_운영진은_전체_스코프의_KPI를_조회한다() {
        AdminDashboardQuery query = AdminDashboardQuery.of(MEMBER_ID, GISU_ID, null, null);
        AdminAnalyticsScope scope = centralScope();
        AdminDashboardSummaryInfo expected = AdminDashboardSummaryInfo.of(
            10L,
            2L,
            100.0,
            3L,
            1L,
            AdminDashboardSummaryInfo.PointSumInfo.of(12L, -4L),
            Map.of()
        );
        given(scopeResolver.resolve(MEMBER_ID, GISU_ID, null, null, null)).willReturn(scope);
        given(loadAdminDashboardAnalyticsPort.getSummary(scope)).willReturn(expected);

        AdminDashboardSummaryInfo actual = sut.getSummary(query);

        assertThat(actual).isEqualTo(expected);
        then(loadAdminDashboardAnalyticsPort).should().getSummary(scope);
    }

    @Test
    @DisplayName("summary 학교 운영진은 본인 학교 데이터만 조회한다")
    void summary_학교_운영진은_본인_학교_데이터만_조회한다() {
        AdminDashboardQuery query = AdminDashboardQuery.of(MEMBER_ID, GISU_ID, null, null);
        AdminAnalyticsScope scope = AdminAnalyticsScope.of(
            AdminAnalyticsScopeType.SCHOOL,
            GISU_ID,
            null,
            30L,
            null,
            ChallengerRoleType.SCHOOL_PRESIDENT
        );
        AdminDashboardSummaryInfo expected = AdminDashboardSummaryInfo.empty();
        given(scopeResolver.resolve(MEMBER_ID, GISU_ID, null, null, null)).willReturn(scope);
        given(loadAdminDashboardAnalyticsPort.getSummary(scope)).willReturn(expected);

        AdminDashboardSummaryInfo actual = sut.getSummary(query);

        assertThat(actual).isEqualTo(expected);
        then(loadAdminDashboardAnalyticsPort).should().getSummary(scope);
    }

    @Test
    @DisplayName("actionQueue 출석 승인 대기와 위험군, 수료 임박 항목을 조회한다")
    void actionQueue_출석_승인_대기와_위험군_수료_임박_항목을_조회한다() {
        AdminDashboardActionQueueQuery query = AdminDashboardActionQueueQuery.of(MEMBER_ID, GISU_ID, -8);
        AdminAnalyticsScope scope = centralScope();
        AdminDashboardActionQueueInfo expected = AdminDashboardActionQueueInfo.of(4L, 3L, 2L);
        given(scopeResolver.resolve(MEMBER_ID, GISU_ID, null, null, null)).willReturn(scope);
        given(loadAdminDashboardAnalyticsPort.getActionQueue(scope, -8)).willReturn(expected);

        AdminDashboardActionQueueInfo actual = sut.getActionQueue(query);

        assertThat(actual.pendingAttendanceDecisionCount()).isEqualTo(4L);
        assertThat(actual.newRiskMemberCountThisWeek()).isEqualTo(3L);
        assertThat(actual.upcomingGraduationCount()).isEqualTo(2L);
        then(loadAdminDashboardAnalyticsPort).should().getActionQueue(scope, -8);
    }

    @Test
    @DisplayName("operationsOverview는 권한 스코프를 적용해 운영 현황을 조회한다")
    void operationsOverview는_권한_스코프를_적용해_운영_현황을_조회한다() {
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-13T00:00:00Z");
        AdminOperationsOverviewQuery query = AdminOperationsOverviewQuery.of(MEMBER_ID, GISU_ID, from, to);
        AdminAnalyticsScope scope = centralScope();
        AdminOperationsOverviewInfo expected = AdminOperationsOverviewInfo.of(
            java.util.List.of(),
            java.util.List.of(),
            AdminOperationsOverviewInfo.ScheduleAttendanceStatusInfo.of(3L, 2L, 8L, Map.of()),
            AdminOperationsOverviewInfo.StudyGroupStatusInfo.of(4L, 6L),
            java.util.List.of(AdminOperationsOverviewInfo.SignupBucketInfo.of(LocalDate.parse("2026-05-01"), 5L))
        );
        given(scopeResolver.resolve(MEMBER_ID, GISU_ID, null, null, null)).willReturn(scope);
        given(loadAdminOperationsAnalyticsPort.getOperationsOverview(scope, query)).willReturn(expected);

        AdminOperationsOverviewInfo actual = sut.getOperationsOverview(query);

        assertThat(actual).isEqualTo(expected);
        then(loadAdminOperationsAnalyticsPort).should().getOperationsOverview(scope, query);
    }

    private AdminAnalyticsScope centralScope() {
        return AdminAnalyticsScope.of(
            AdminAnalyticsScopeType.CENTRAL,
            GISU_ID,
            null,
            null,
            null,
            ChallengerRoleType.CENTRAL_PRESIDENT
        );
    }
}
