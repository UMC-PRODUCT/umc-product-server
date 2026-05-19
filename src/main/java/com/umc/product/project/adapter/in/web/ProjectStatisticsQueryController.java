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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
        description = """
            차수별 · 학교별 · 프로젝트×차수별 지원 집계를 반환합니다.

            **호출자 역할에 따른 집계 범위 분기**
            - 운영진(ChallengerRole 보유): gisuId + chapterId 범위 전체 프로젝트 기준 집계. schoolStats.total(학교 전체 챌린저 수) 제공.
            - PM챌린저(role 없음): 호출자가 소유한 프로젝트만 집계. schoolStats.total = null.
            - 그 외(일반 챌린저): 403 반환.

            **roundStats.quota**: ADMIN 파트를 제외한 gisuId 기준 전체 챌린저 수.
            **schoolStats.count**: 해당 학교 소속 챌린저 중 해당 차수에 지원한 고유 인원 수(중복 지원 제거).
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "지원통계 조회 권한이 없습니다."
    )
    public ApplicationStatisticsResponse getApplicationStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "기수 ID", required = true) @RequestParam Long gisuId,
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.applicationStatsFor(gisuId, chapterId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics/matchings")
    @Operation(
        summary = "[PROJECT-STAT-003/004] 매칭통계 조회",
        description = """
            차수별 · 학교별 · 프로젝트×차수별 매칭 집계를 반환합니다.

            **호출자 역할에 따른 집계 범위 분기**
            - 운영진(ChallengerRole 보유): gisuId + chapterId 범위 전체 프로젝트 기준 집계. schoolStats.total(학교 전체 챌린저 수) 제공.
            - PM챌린저(role 없음): 호출자가 소유한 프로젝트만 집계. schoolStats.total = null.
            - 그 외(일반 챌린저): 403 반환.

            **주의**: application=null인 랜덤 매칭 멤버는 차수 정보가 없으므로 집계에서 제외됩니다.
            **roundStats.quota**: ADMIN 파트를 제외한 gisuId 기준 전체 챌린저 수.
            **schoolStats.count**: 해당 학교 소속 챌린저 중 해당 차수에 매칭된 인원 수.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "매칭통계 조회 권한이 없습니다."
    )
    public MatchingStatisticsResponse getMatchingStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "기수 ID", required = true) @RequestParam Long gisuId,
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.matchingStatsFor(gisuId, chapterId, memberPrincipal.getMemberId());
    }
}
