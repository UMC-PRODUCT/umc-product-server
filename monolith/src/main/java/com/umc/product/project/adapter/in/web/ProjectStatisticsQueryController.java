package com.umc.product.project.adapter.in.web;

import java.util.List;

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
        operationId = "PROJECT-STAT-001",
        summary = "단건 프로젝트 지원/매칭 현황 조회 (Deprecated)",
        deprecated = true,
        description = """
            deprecated: `/api/v1/projects/statistics/applications?projectIds={projectId}`를 사용해주세요.

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

    @GetMapping("/statistics/applications")
    @Operation(
        operationId = "PROJECT-STAT-002",
        summary = "프로젝트 지원 현황 조회",
        description = """
            디자인·개발 매칭의 차수별·학교별·프로젝트별 지원 현황을 조회합니다.
            projectIds와 chapterId 중 하나만 보내야 하며, 둘 다 보내거나 둘 다 없으면 400을 반환합니다.

            조회 범위
            - chapterId: 해당 지부의 모든 프로젝트를 집계합니다.
            - projectIds: 지정한 프로젝트만 집계합니다. 단건은 값 1개로 요청하며, 모두 같은 기수·지부여야 합니다(아니면 400 PROJECT-0501).
            - 임시저장(DRAFT)·승인대기(PENDING_REVIEW) 프로젝트는 지원서를 받을 수 없으므로 항상 제외합니다.
              중단(ABORTED)·완료(COMPLETED) 프로젝트는 이전에 받은 지원서가 있으므로 포함합니다.

            기준 매칭 (matchingRound)
            - 지원 기간(startsAt~endsAt)이 진행 중인 1~3차를 기준으로 하며, 없으면 가장 최근에 종료된 1~3차를 사용합니다.
            - 기준 차수와 같은 유형(PLAN_DESIGN 또는 PLAN_DEVELOPER)의 1~3차만 집계합니다. 랜덤 매칭은 지원서를 받지 않아 제외합니다.
            - 시작한 1~3차가 없으면 matchingRound는 null이고 summary의 배열은 모두 비어 있습니다.

            집계 기준
            - 지원 인원은 제출·합격·불합격 지원서를 낸 인원이며, 같은 차수에서 여러 번 지원해도 한 명으로 셉니다.
            - 지원 가능 인원은 지부의 기준 유형 활동 중 챌린저 수입니다. projectIds로 조회해도 지부 기준으로 계산합니다.
            - summary.roundApplicationStatistics: 차수별 지원 인원과 지원 가능 인원입니다. 지원 가능 인원은 이전 차수까지 매칭된 인원을 뺀 값이며,
              지원률은 appliedMemberCount / availableMemberCount로 계산합니다.
            - summary.schoolApplicationStatistics: 학교별 지원 가능 총원과 1·2·3차 지원 인원입니다. 총원은 차수가 지나도 차감하지 않습니다.
            - summary.projectRoundStatistics: 프로젝트별 1·2·3차 지원 인원입니다. 팀 매칭 프로젝트 목록과 같이 등록순으로 정렬합니다.
            - 학교별·프로젝트별 rounds는 기준 유형에 생성된 1~3차만 반환하며, 지원자가 없는 차수도 0명으로 반환합니다.
            - 개인 정보와 지원자 명단은 포함하지 않습니다.

            함께 사용하는 API
            - 프로젝트 목록·모집 상태·파트별 현재 인원/TO: GET /api/v1/projects
            - 프로젝트별 지원자 목록(아코디언): GET /api/v1/projects/{projectId}/applications,
              GET /api/v1/projects/applications?projectIds={projectIds}
            - 공개 매칭 통계: GET /api/v1/projects/statistics/matchings?chapterId={chapterId}

            권한
            - SUPER_ADMIN, 해당 기수 총괄단, 해당 지부장, 해당 지부 소속 학교 회장·부회장은 chapterId·projectIds 모두 조회할 수 있습니다.
            - PO·Sub-PM은 본인 프로젝트만 projectIds로 조회할 수 있습니다.
            - 권한이 없으면 403, 존재하지 않는 프로젝트가 포함되면 404를 반환합니다.
            """
    )
    public ChapterProjectStatisticsResponse getStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID 목록. chapterId와 동시에 제공할 수 없습니다. 단건 조회는 값 1개로 요청합니다.")
        @RequestParam(required = false) List<Long> projectIds,
        @Parameter(description = "지부 ID. projectIds와 동시에 제공할 수 없습니다.")
        @RequestParam(required = false) Long chapterId
    ) {
        validateSingleStatisticsTarget(projectIds, chapterId);
        if (projectIds != null) {
            return assembler.statisticsForProjects(projectIds, memberPrincipal.getMemberId());
        }
        return assembler.statisticsForChapter(chapterId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics/matchings")
    @Operation(
        operationId = "PROJECT-STAT-003",
        summary = "지부 공개 프로젝트 매칭 요약 조회",
        description = """
            로그인 사용자 누구나 조회할 수 있는 지부 내 공개 프로젝트 매칭 통계입니다.
            멤버 ID와 지원서 ID는 노출하지 않고 집계 숫자만 반환합니다.

            집계 대상 프로젝트는 공개 프로젝트 목록과 동일하게 IN_PROGRESS, COMPLETED 상태만 포함합니다.
            매칭 유형(PLAN_DESIGN 또는 PLAN_DEVELOPER)은 지원 통계와 같은 기준 차수로 정합니다.
            진행 중인 1~3차가 있으면 그 차수를, 없으면 가장 최근에 종료된 1~3차를 기준으로 합니다. 기준 차수가 없으면 모든 배열이 비어 있습니다.

            집계 기준
            - 기준 유형의 1~3차와 랜덤 매칭을 집계합니다.
            - 매칭 인원은 결과가 공개된 팀원을 배정된 차수 기준으로 셉니다. 배정된 차수가 없는 팀원은 제외합니다.
            - 매칭 대상은 해당 기수·매칭 지부에서 활동 중인 기준 유형 챌린저입니다.
            - 차수 배열은 생성된 차수만 차수 순서로 반환하며, 매칭 인원이 없어도 0명으로 반환합니다.

            응답 항목
            - roundMatchingStatistics: 차수별 매칭 인원과 매칭 대기 인원입니다. 매칭 대기 인원은 이전 차수까지 매칭된 인원을 뺀 값입니다.
            - schoolMatchingStatistics: 학교별 누적 매칭 인원과 매칭 대상 총원입니다. 총원은 차수가 지나도 차감하지 않습니다.
            - projectMatchingStatistics: 프로젝트별 누적 매칭 인원, 전체 TO, 차수별·파트별 매칭 인원과 TO입니다.
            """
    )
    public ChapterProjectMatchingStatisticsResponse getPublicMatchingStatistics(
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.matchingStatisticsForChapter(chapterId);
    }

    private void validateSingleStatisticsTarget(List<Long> projectIds, Long chapterId) {
        boolean hasProjectIds = projectIds != null && !projectIds.isEmpty();
        boolean hasChapterId = chapterId != null;
        int targetCount = (hasProjectIds ? 1 : 0) + (hasChapterId ? 1 : 0);

        if (targetCount != 1) {
            throw new CommonException(
                CommonErrorCode.BAD_REQUEST,
                "projectIds 또는 chapterId 중 하나만 제공해주세요."
            );
        }
    }
}
