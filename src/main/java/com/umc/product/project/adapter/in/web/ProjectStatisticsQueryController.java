package com.umc.product.project.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ChapterProjectMatchingStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsQueryResponse;

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
        operationId = "PROJECT-STAT-001",
        summary = "단건 프로젝트 지원/매칭 현황 조회 (Deprecated)",
        deprecated = true,
        description = """
            deprecated: `/api/v1/projects/statistics?projectId={projectId}`를 사용해주세요.

            프로젝트 ID와 함께 멤버 목록을 포함하고 있고, FE단 재가공을 최소화해드리기 위해서 `roundApplicationStatistics` 및 `schoolApplicationStatistics` 필드를 두고 있습니다.

            각 항목이 매칭 차수별 지원률, N차 매칭에서의 학교별 및 총 지원자 수를 포함하고 있습니다.
            프로젝트 멤버 목록의 경우 각 멤버가 해당 프로젝트에 작성한 지원 이력을 포함하고 있으며,
            없거나 (강제배정/랜덤매칭) 여러 건 (떨어지고 재 지원하는 경우) 이 존재할 수 있어 배열로 구성되어 있습니다.

            지원자 목록은(특정 프로젝트에 대한 지원서 조회) `/api/v1/projects/{projectId}/applications`를 호출하셔서 활용하셔야 합니다.

            권한: 해당 프로젝트의 PO/Sub-PM(본인 프로젝트), 총괄단, 해당 지부장, 해당 지부 소속 학교 회장/부회장만 조회할 수 있습니다. 그 외에는 403.
            """
    )
    @Deprecated(since = "v2.0.0", forRemoval = true)
    public ProjectStatisticsResponse getProjectStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID", required = true) @PathVariable Long projectId
    ) {
        return assembler.statisticsForProject(projectId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics")
    @Operation(
        operationId = "PROJECT-STAT-002",
        summary = "프로젝트 지원/매칭 현황 통합 조회",
        description = """
            projectId 또는 chapterId 중 정확히 하나만 query parameter로 제공해야 합니다.

            - projectId 제공: 단건 프로젝트 지원/매칭 현황을 반환합니다.
            - chapterId 제공: 지부 전체 프로젝트 지원/매칭 현황을 반환합니다.

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

            권한: 총괄단(모든 지부), 해당 지부장, 해당 지부 소속 학교 회장/부회장만 조회할 수 있습니다. 그 외에는 403. (PO/Sub-PM 은 본인 프로젝트를 단건 조회 API로 확인합니다.)
            """
    )
    public ProjectStatisticsQueryResponse getStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID. chapterId와 동시에 제공할 수 없습니다.")
        @RequestParam(required = false) Long projectId,
        @Parameter(description = "지부 ID. projectId와 동시에 제공할 수 없습니다.")
        @RequestParam(required = false) Long chapterId
    ) {
        validateSingleStatisticsTarget(projectId, chapterId);
        if (projectId != null) {
            return assembler.statisticsForProject(projectId, memberPrincipal.getMemberId());
        }
        return assembler.statisticsForChapter(chapterId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics/matchings")
    @Operation(
        operationId = "PROJECT-STAT-003",
        summary = "지부 공개 프로젝트 매칭 요약 조회",
        description = """
            로그인 사용자 누구나 조회할 수 있는 지부 내 공개 프로젝트 매칭 요약입니다.
            멤버 ID와 지원서 ID는 노출하지 않고 ProjectMember 기준 집계 숫자만 반환합니다.

            집계 대상 프로젝트는 공개 프로젝트 목록과 동일하게 IN_PROGRESS, COMPLETED 상태만 포함합니다.
            차수 귀속은 각 ProjectMember가 해당 프로젝트에 작성한 APPROVED 지원서 중 가장 오래된 매칭 차수를 사용합니다.
            합격 지원서가 없으면 차수별 집계에서 제외하고 unclassifiedMatchingStatistics에 별도 집계합니다.
            """
    )
    public ChapterProjectMatchingStatisticsResponse getPublicMatchingStatistics(
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.matchingStatisticsForChapter(chapterId);
    }

    private void validateSingleStatisticsTarget(Long projectId, Long chapterId) {
        if ((projectId == null) == (chapterId == null)) {
            throw new CommonException(
                CommonErrorCode.BAD_REQUEST,
                "projectId 또는 chapterId 중 하나만 제공해주세요."
            );
        }
    }
}
