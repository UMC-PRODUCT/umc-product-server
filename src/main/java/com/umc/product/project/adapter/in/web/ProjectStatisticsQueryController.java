package com.umc.product.project.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 통계 Query", description = "프로젝트 지원·매칭 현황 조회")
public class ProjectStatisticsQueryController {

    private final ProjectResponseAssembler assembler;

    @GetMapping("/{projectId}/statistics")
    @Operation(
        summary = "[PROJECT-STAT-001] 단건 프로젝트 지원/매칭 현황 조회",
        description = """
            프로젝트 ID와 함께 멤버 목록을 포함하고 있고, FE단 재가공을 최소화해드리기 위해서 `roundApplicationStatistics` 및 `schoolApplicationStatistics` 필드를 두고 있습니다.

            각 항목이 매칭 차수별 지원률, N차 매칭에서의 학교별 및 총 지원자 수를 포함하고 있습니다.
            프로젝트 멤버 목록의 경우 각 멤버가 해당 프로젝트에 작성한 지원 이력을 포함하고 있으며,
            없거나 (강제배정/랜덤매칭) 여러 건 (떨어지고 재 지원하는 경우) 이 존재할 수 있어 배열로 구성되어 있습니다.

            지원자 목록은(특정 프로젝트에 대한 지원서 조회) `/api/v1/projects/{projectId}/applications`를 호출하셔서 활용하셔야 합니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        resourceId = "#projectId",
        permission = PermissionType.READ,
        message = "프로젝트 지원/매칭 현황을 볼 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    public ProjectStatisticsResponse getProjectStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId
    ) {
        return assembler.statisticsForProject(projectId);
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "[PROJECT-STAT-002] 지부 전체 프로젝트 지원/매칭 현황 조회",
        description = """
            chapterId에 속한 전체 프로젝트를 대상으로 ACTIVE ProjectMember 목록과
            각 멤버가 해당 프로젝트에 작성한 지원 이력을 프로젝트별로 반환합니다.
            summary에는 차수별 지원 완료 인원/지원 가능 총원, 학교 순위, 학교별 매칭 인원,
            프로젝트별 차수 인원 수를 함께 반환합니다.

            지부 내 회장단 이상의 운영진이 매칭 통계를 조회할 때 활용합니다.

            지부 내 모든 프로젝트 목록 및 각 프로젝트에 대한 프로젝트 멤버를 포함하고 있으며,
            이는 `/api/v1/projects/{projectId}/statistics` 에서 제공하는 것과 동일한 형태입니다.

            추가로 BFF 패턴을 적용하여 FE단 데이터 가공 책임을 줄이기 위해 `summary` 필드를 제공하고 있습니다.

            - roundApplicationStatistics: N차 매칭 지원 현황 카드에 활용합니다. 각 매칭 차수별 정보 (매칭 종류 및 차수) 와 각 차수별 지원자 수 & 지원 가능했던 인원
            - roundSchoolRankings: N차 매칭 지원 Top N에 활용합니다. 각 차수별로, 각 학교별 지원자 수
            - schoolMatchingStatistics: 총원 N명 카드에 활용합니다. 차수와 무관하게, 각 학교별 총 매칭 완료 인원 & 지원 가능 총원
            - projectRoundStatistics: 프로젝트별 지원 현황 필드에 활용합니다. 각 프로젝트별로, 각 매칭 차수별 정보 (매칭 종류 & 차수) 와 지원자 수
            """
    )
    @CheckAccess(
        resourceType = ResourceType.PROJECT,
        permission = PermissionType.READ,
        message = "프로젝트 지원/매칭 현황을 볼 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    public ChapterProjectStatisticsResponse listChapterProjectStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.statisticsForChapter(chapterId);
    }
}
