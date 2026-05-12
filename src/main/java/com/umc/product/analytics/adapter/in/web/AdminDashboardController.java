package com.umc.product.analytics.adapter.in.web;

import com.umc.product.analytics.adapter.in.web.dto.request.AdminDashboardActionQueueRequest;
import com.umc.product.analytics.adapter.in.web.dto.request.AdminDashboardRequest;
import com.umc.product.analytics.adapter.in.web.dto.request.AdminOperationsOverviewRequest;
import com.umc.product.analytics.adapter.in.web.dto.request.AdminRiskChallengerRequest;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminDashboardActionQueueResponse;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminDashboardContextResponse;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminDashboardSummaryResponse;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminOperationsOverviewResponse;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminRiskChallengerResponse;
import com.umc.product.analytics.application.port.in.query.GetAdminDashboardActionQueueUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminDashboardContextUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminDashboardSummaryUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminOperationsOverviewUseCase;
import com.umc.product.analytics.application.port.in.query.GetAdminRiskChallengerUseCase;
import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard | 운영진 대시보드", description = "운영진 메인 대시보드 집계 API")
public class AdminDashboardController {

    private final GetAdminDashboardSummaryUseCase getAdminDashboardSummaryUseCase;
    private final GetAdminDashboardActionQueueUseCase getAdminDashboardActionQueueUseCase;
    private final GetAdminDashboardContextUseCase getAdminDashboardContextUseCase;
    private final GetAdminRiskChallengerUseCase getAdminRiskChallengerUseCase;
    private final GetAdminOperationsOverviewUseCase getAdminOperationsOverviewUseCase;

    @Operation(summary = "[ADMIN-DASHBOARD-001] 운영진 대시보드 요약 조회")
    @GetMapping("summary")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public AdminDashboardSummaryResponse getSummary(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject AdminDashboardRequest request
    ) {
        return AdminDashboardSummaryResponse.from(
            getAdminDashboardSummaryUseCase.getSummary(request.toQuery(memberPrincipal.getMemberId()))
        );
    }

    @Operation(summary = "[ADMIN-DASHBOARD-002] 운영진 대시보드 액션 큐 조회")
    @GetMapping("action-queue")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public AdminDashboardActionQueueResponse getActionQueue(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject AdminDashboardActionQueueRequest request
    ) {
        return AdminDashboardActionQueueResponse.from(
            getAdminDashboardActionQueueUseCase.getActionQueue(request.toQuery(memberPrincipal.getMemberId()))
        );
    }

    @Operation(summary = "[ADMIN-DASHBOARD-004] 운영진 대시보드 권한 컨텍스트 조회")
    @GetMapping("context")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public AdminDashboardContextResponse getContext(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return AdminDashboardContextResponse.from(
            getAdminDashboardContextUseCase.getContext(memberPrincipal.getMemberId())
        );
    }

    @Operation(summary = "[ADMIN-DASHBOARD-005] 운영 현황 집계 조회")
    @GetMapping("operations")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public AdminOperationsOverviewResponse getOperationsOverview(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject AdminOperationsOverviewRequest request
    ) {
        return AdminOperationsOverviewResponse.from(
            getAdminOperationsOverviewUseCase.getOperationsOverview(request.toQuery(memberPrincipal.getMemberId()))
        );
    }

    @Operation(summary = "[ADMIN-DASHBOARD-003] 운영진 대시보드 위험군 챌린저 조회")
    @GetMapping("risk-challengers")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public PageResponse<AdminRiskChallengerResponse> getRiskChallengers(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject Pageable pageable,
        @ParameterObject AdminRiskChallengerRequest request
    ) {
        return PageResponse.of(
            getAdminRiskChallengerUseCase.getRiskChallengers(request.toQuery(memberPrincipal.getMemberId(), pageable)),
            AdminRiskChallengerResponse::from
        );
    }
}
