package com.umc.product.project.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.MatchingStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 통계 Query", description = "프로젝트 지원·매칭 통계 조회")
public class ProjectStatisticsQueryController {

    private final ProjectResponseAssembler assembler;

    @GetMapping("/statistics/applications")
    @Operation(
        summary = "[PROJECT-STAT-001/002] 지원통계 조회",
        description = "운영진은 지부 전체, PM챌린저는 본인 프로젝트 범위로 차수별/학교별/프로젝트×차수별 지원 집계를 반환합니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "지원통계 조회 권한이 없습니다."
    )
    public ApplicationStatisticsResponse getApplicationStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @RequestParam Long chapterId
    ) {
        return assembler.applicationStatsFor(gisuId, chapterId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics/matchings")
    @Operation(
        summary = "[PROJECT-STAT-003/004] 매칭통계 조회",
        description = "운영진은 지부 전체, PM챌린저는 본인 프로젝트 범위로 차수별/학교별/프로젝트×차수별 매칭 집계를 반환합니다."
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "매칭통계 조회 권한이 없습니다."
    )
    public MatchingStatisticsResponse getMatchingStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @RequestParam Long chapterId
    ) {
        return assembler.matchingStatsFor(gisuId, chapterId, memberPrincipal.getMemberId());
    }
}
