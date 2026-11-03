package com.umc.product.project.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingOverviewResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectApplicationStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectMatchingStatisticsResponse;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;

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

    @GetMapping("/statistics/applications")
    @Operation(
        operationId = "PROJECT-STAT-002",
        summary = "프로젝트 지원 통계 조회",
        description = """
            디자인·개발 매칭의 차수별·학교별·프로젝트별 지원 현황을 조회합니다.
            chapterId와 projectIds 중 하나만 보내야 하며, 둘 다 보내거나 둘 다 없으면 400을 반환합니다.

            화면에서 함께 사용하는 API
            - 상단 지원 통계 그래프: 이 API를 사용합니다.
            - 하단 프로젝트 목록: `GET /api/v1/projects?gisuId=...&chapterId=...` (PROJECT-001)로 조회합니다.
            - 프로젝트 지원자 목록: `GET /api/v1/projects/applications?projectIds=...` (APPLY-101-BATCH) 또는 `GET /api/v1/projects/{projectId}/applications` (APPLY-101)을 사용합니다.

            조회 범위
            - chapterId: 해당 지부의 프로젝트를 모두 집계합니다.
            - projectIds: 지정한 프로젝트만 집계합니다. 모두 같은 기수·지부여야 하며, 아니면 400(PROJECT-0501)을 반환합니다.
            - 임시저장(DRAFT)·승인대기(PENDING_REVIEW) 프로젝트는 제외하고, 중단(ABORTED)·완료(COMPLETED) 프로젝트는 포함합니다.

            기준 차수(matchingRound)
            - 진행 중인 1·2·3차가 있으면 그 차수를, 없으면 가장 최근에 종료된 1·2·3차를 기준으로 합니다.
            - 기준 차수와 같은 유형(PLAN_DESIGN 또는 PLAN_DEVELOPER)의 1·2·3차만 집계합니다.
            - 시작한 1·2·3차가 없으면 matchingRound는 null이고 모든 배열이 비어 있습니다.

            집계 기준
            - 지원 인원은 제출·합격·불합격 지원서를 낸 인원이며, 같은 차수에 여러 번 지원해도 1명으로 셉니다.
            - 지원 가능 인원은 해당 지부에서 활동 중인 기준 유형 챌린저 수입니다. projectIds로 조회해도 지부 기준으로 계산합니다.
            - 차수 배열은 생성된 1·2·3차만 차수 순서로 반환하며, 지원자가 없어도 0명으로 반환합니다.

            응답 항목
            - summary.roundApplicationStatistics: 차수별 지원 인원과 지원 가능 인원입니다. 지원 가능 인원은 이전 차수까지 매칭된 인원을 뺀 값입니다.
            - summary.schoolApplicationStatistics: 학교별 지원 가능 총원과 차수별 지원 인원입니다. 총원은 차수가 지나도 차감하지 않습니다.
            - summary.projectRoundStatistics: 프로젝트별 차수별 지원 인원입니다. 프로젝트 등록순으로 정렬합니다.

            권한
            - SUPER_ADMIN, 해당 기수 총괄단, 해당 지부장, 해당 지부 소속 학교 회장·부회장은 chapterId·projectIds 모두 조회할 수 있습니다.
            - PO·Sub-PM은 본인 프로젝트만 projectIds로 조회할 수 있습니다.
            - 권한이 없으면 403, 존재하지 않는 프로젝트가 포함되면 404를 반환합니다.
            """
    )
    public ProjectApplicationStatisticsResponse getApplicationStatistics(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "프로젝트 ID 목록. chapterId와 동시에 제공할 수 없습니다. 단건 조회는 값 1개로 요청합니다.")
        @RequestParam(required = false) List<Long> projectIds,
        @Parameter(description = "지부 ID. projectIds와 동시에 제공할 수 없습니다.")
        @RequestParam(required = false) Long chapterId
    ) {
        validateSingleStatisticsTarget(projectIds, chapterId);
        if (projectIds != null) {
            return assembler.applicationStatisticsForProjects(projectIds, memberPrincipal.getMemberId());
        }
        return assembler.applicationStatisticsForChapter(chapterId, memberPrincipal.getMemberId());
    }

    @GetMapping("/statistics/matchings")
    @Operation(
        operationId = "PROJECT-STAT-003",
        summary = "프로젝트 매칭 통계 조회",
        description = """
            로그인 사용자 누구나 조회할 수 있는 지부 내 공개 프로젝트 매칭 통계입니다.
            멤버 ID와 지원서 ID는 노출하지 않고 집계 숫자만 반환합니다.

            화면에서 함께 사용하는 API
            - 상단 매칭 통계 그래프: 이 API를 사용합니다.
            - 하단 프로젝트별 매칭 명단·인원/TO: `GET /api/v1/projects/statistics/matching-overview?chapterId=...` (PROJECT-STAT-004)를 사용합니다.
            - 운영진의 하단 조정 명단은 PROJECT-STAT-004에 `view=ADJUSTMENT`를 추가해 조회합니다. 상단 통계는 공개 결과를 기준으로 집계합니다.

            집계 대상 프로젝트는 공개 프로젝트 목록과 동일하게 IN_PROGRESS, COMPLETED 상태만 포함합니다.
            집계할 매칭 유형(PLAN_DESIGN 또는 PLAN_DEVELOPER)은 지원 통계와 동일하게 선택합니다.
            진행 중인 1·2·3차의 유형을 사용하고, 없으면 가장 최근 종료된 1·2·3차의 유형을 사용합니다. 시작한 1·2·3차가 없으면 모든 배열이 비어 있습니다.
            선택한 유형의 생성된 1·2·3차와 랜덤 매칭 전체를 집계합니다.

            집계 기준
            - 기준 유형의 1·2·3차와 랜덤 매칭을 집계합니다.
            - 매칭 인원은 결과가 공개된 팀원을 배정된 차수 기준으로 셉니다. 배정된 차수가 없는 팀원은 제외합니다.
            - 매칭 대상은 해당 기수·매칭 지부에서 활동 중인 기준 유형 챌린저입니다.
            - 차수 배열은 생성된 차수만 차수 순서로 반환하며, 매칭 인원이 없어도 0명으로 반환합니다.

            응답 항목
            - roundMatchingStatistics: 차수별 매칭 인원과 매칭 대기 인원입니다. 매칭 대기 인원은 이전 차수까지 매칭된 인원을 뺀 값입니다.
            - schoolMatchingStatistics: 학교별 누적 매칭 인원과 매칭 대상 총원입니다. 총원은 차수가 지나도 차감하지 않습니다.
            - projectMatchingStatistics: 프로젝트별 누적 매칭 인원, 전체 TO, 차수별·파트별 매칭 인원과 TO입니다.
            """
    )
    public ProjectMatchingStatisticsResponse getMatchingStatistics(
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId
    ) {
        return assembler.matchingStatisticsForChapter(chapterId);
    }

    @GetMapping("/statistics/matching-overview")
    @Operation(
        operationId = "PROJECT-STAT-004",
        summary = "프로젝트 매칭 결과 시트 조회",
        description = """
            지부의 공개 프로젝트(IN_PROGRESS, COMPLETED)별 팀원 명단과 인원/TO를 조회합니다.

            - 인원과 TO는 디자인·개발 파트만 집계합니다. (PM·보조 PM 제외)
            - IN_PROGRESS 프로젝트는 활동 중(ACTIVE) 팀원을, COMPLETED 프로젝트는 활동을 마친(COMPLETED) 팀원을 포함합니다.
            - 운영진 화면에서는 view=ADJUSTMENT로 요청해 주세요. 랜덤 매칭 후 최종 결과 공개 전까지는 어드민이 조정 중인 명단을, 그 외 시점에는 공개된 명단을 반환합니다.
            - view=ADJUSTMENT는 SUPER_ADMIN, 해당 기수 총괄단, 해당 지부장만 요청할 수 있습니다. (그 외 403)
            """
    )
    public ProjectMatchingOverviewResponse getMatchingOverview(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "지부 ID", required = true) @RequestParam Long chapterId,
        @Parameter(description = "명단 기준. PUBLISHED(기본값) 또는 ADJUSTMENT")
        @RequestParam(defaultValue = "PUBLISHED") ProjectMemberView view
    ) {
        return assembler.matchingOverviewForChapter(chapterId, view, memberPrincipal.getMemberId());
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
