package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMatchingRoundStatisticsResponse;
import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.RoundApplicationStatisticsResponse;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.PhaseApplicantCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ProjectRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SchoolStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.SummaryInfo;
import com.umc.product.project.domain.enums.MatchingPhase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 지원 통계. chapterId 조회는 지부 전체, projectIds 조회는 지정한 프로젝트를 집계합니다.")
public record ChapterProjectStatisticsResponse(
    @Schema(description = "집계 기준 지부 ID. projectIds 조회 시에는 지정한 프로젝트들의 지부입니다.", example = "1")
    Long chapterId,
    @Schema(description = """
        통계 기준 매칭 차수. 지원 기간이 진행 중인 1~3차이며, 없으면 가장 최근에 종료된 1~3차입니다.
        type으로 디자인(PLAN_DESIGN)·개발(PLAN_DEVELOPER) 중 어느 매칭의 통계인지 확인합니다.
        시작한 1~3차가 없으면 null입니다.""", nullable = true)
    ProjectMatchingRoundStatisticsResponse matchingRound,
    @Schema(description = "기준 매칭 유형의 차수별·학교별·프로젝트별 지원 통계. matchingRound가 null이면 모든 배열이 비어 있습니다.")
    ChapterProjectStatisticsSummaryResponse summary
) {
    public static ChapterProjectStatisticsResponse from(ProjectApplicationStatisticsInfo info) {
        return new ChapterProjectStatisticsResponse(
            info.chapterId(),
            info.matchingRound() == null ? null : ProjectMatchingRoundStatisticsResponse.from(info.matchingRound()),
            ChapterProjectStatisticsSummaryResponse.from(info.summary())
        );
    }

    @Schema(description = "지원 통계 요약")
    public record ChapterProjectStatisticsSummaryResponse(
        @Schema(description = """
            차수별 지원 인원과 지원 가능 인원. 기준 매칭 유형으로 등록된 1~3차만 phase 순으로 반환합니다.
            appliedMemberCount는 해당 차수에 지원한 인원, availableMemberCount는 지부의 해당 유형 전체 인원에서
            이전 차수까지 매칭된 인원을 뺀 값입니다. 지원률은 appliedMemberCount / availableMemberCount로 계산합니다.""")
        List<RoundApplicationStatisticsResponse> roundApplicationStatistics,
        @Schema(description = "학교별 지원 가능 총원과 1·2·3차 지원 인원. 지원자가 없는 학교도 포함하며 schoolId 오름차순입니다.")
        List<SchoolApplicationStatisticsResponse> schoolApplicationStatistics,
        @Schema(description = """
            프로젝트별 1·2·3차 지원 인원. 지원자가 없는 프로젝트도 포함하며, 팀 매칭 프로젝트 목록과 같이 등록순(PM 제출 시각 오름차순)입니다.
            임시저장(DRAFT)·승인대기(PENDING_REVIEW) 프로젝트는 projectIds로 지정해도 제외합니다.""")
        List<ProjectRoundStatisticsResponse> projectRoundStatistics
    ) {
        private static ChapterProjectStatisticsSummaryResponse from(SummaryInfo info) {
            return new ChapterProjectStatisticsSummaryResponse(
                info.roundApplicationStatistics().stream().map(RoundApplicationStatisticsResponse::from).toList(),
                info.schoolApplicationStatistics().stream().map(SchoolApplicationStatisticsResponse::from).toList(),
                info.projectRoundStatistics().stream().map(ProjectRoundStatisticsResponse::from).toList());
        }
    }

    @Schema(description = "학교별 지원 현황")
    public record SchoolApplicationStatisticsResponse(
        @Schema(description = "학교 ID", example = "3")
        Long schoolId,
        @Schema(description = "학교명", example = "홍익대학교")
        String schoolName,
        @Schema(description = "기준 매칭 유형의 학교별 지원 가능 총원. 차수가 지나도 차감하지 않습니다.", example = "12")
        long totalMemberCount,
        @Schema(description = "1·2·3차 지원 인원. 항상 FIRST, SECOND, THIRD 순서로 3개를 반환합니다.")
        List<PhaseApplicantCountResponse> rounds
    ) {
        private static SchoolApplicationStatisticsResponse from(SchoolStatisticsInfo info) {
            return new SchoolApplicationStatisticsResponse(info.schoolId(), info.schoolName(), info.totalMemberCount(),
                info.rounds().stream().map(PhaseApplicantCountResponse::from).toList());
        }
    }

    @Schema(description = "프로젝트별 지원 현황")
    public record ProjectRoundStatisticsResponse(
        @Schema(description = "프로젝트 ID", example = "100")
        Long projectId,
        @Schema(description = "프로젝트명")
        String projectName,
        @Schema(description = "1·2·3차 지원 인원. 항상 FIRST, SECOND, THIRD 순서로 3개를 반환합니다.")
        List<PhaseApplicantCountResponse> rounds
    ) {
        private static ProjectRoundStatisticsResponse from(ProjectRoundStatisticsInfo info) {
            return new ProjectRoundStatisticsResponse(info.projectId(), info.projectName(),
                info.rounds().stream().map(PhaseApplicantCountResponse::from).toList());
        }
    }

    @Schema(description = "차수별 지원 인원")
    public record PhaseApplicantCountResponse(
        @Schema(description = "매칭 차수", example = "FIRST")
        MatchingPhase phase,
        @Schema(description = "해당 차수에 지원한 인원. 같은 사람이 여러 번 지원해도 한 명으로 셉니다.", example = "5")
        long applicantCount
    ) {
        private static PhaseApplicantCountResponse from(PhaseApplicantCountInfo info) {
            return new PhaseApplicantCountResponse(info.phase(), info.applicantCount());
        }
    }
}
