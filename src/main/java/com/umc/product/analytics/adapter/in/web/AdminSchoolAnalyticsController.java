package com.umc.product.analytics.adapter.in.web;

import com.umc.product.analytics.adapter.in.web.dto.request.AdminSchoolSummaryRequest;
import com.umc.product.analytics.adapter.in.web.dto.response.AdminSchoolSummaryResponse;
import com.umc.product.analytics.application.port.in.query.GetAdminSchoolSummaryUseCase;
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
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
@Tag(name = "Analytics | 학교별 현황", description = "운영진 학교별 현황 집계 API")
public class AdminSchoolAnalyticsController {

    private final GetAdminSchoolSummaryUseCase getAdminSchoolSummaryUseCase;

    @GetMapping("summary")
    @Operation(summary = "[DASHBOARD-100] 학교별 현황 조회")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    public PageResponse<AdminSchoolSummaryResponse> getSchoolSummaries(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject Pageable pageable,
        @ParameterObject AdminSchoolSummaryRequest request
    ) {
        return PageResponse.of(
            getAdminSchoolSummaryUseCase.getSchoolSummaries(request.toQuery(memberPrincipal.getMemberId(), pageable)),
            AdminSchoolSummaryResponse::from
        );
    }
}
