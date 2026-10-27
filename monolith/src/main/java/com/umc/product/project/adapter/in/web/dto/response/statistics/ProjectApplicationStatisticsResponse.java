package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ApplicationPhaseCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ApplicationProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ApplicationSchoolStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectApplicationStatisticsInfo.ApplicationSummaryInfo;
import com.umc.product.project.domain.enums.MatchingPhase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProjectApplicationStatisticsResponse", description = "프로젝트 지원 통계 응답")
public record ProjectApplicationStatisticsResponse(
    @Schema(description = "지부 ID")
    Long chapterId,
    @Schema(description = "통계 기준 매칭 차수. 시작한 1~3차가 없으면 null입니다.", nullable = true)
    ProjectMatchingRoundStatisticsResponse matchingRound,
    @Schema(description = "차수별·학교별·프로젝트별 지원 현황")
    ApplicationSummaryResponse summary
) {
    public static ProjectApplicationStatisticsResponse from(ProjectApplicationStatisticsInfo info) {
        return new ProjectApplicationStatisticsResponse(
            info.chapterId(),
            info.matchingRound() == null ? null : ProjectMatchingRoundStatisticsResponse.from(info.matchingRound()),
            ApplicationSummaryResponse.from(info.summary())
        );
    }

    @Schema(name = "ProjectApplicationSummaryResponse", description = "차수별·학교별·프로젝트별 지원 현황")
    public record ApplicationSummaryResponse(
        @Schema(description = "차수별 지원 현황")
        List<RoundApplicationStatisticsResponse> roundApplicationStatistics,
        @Schema(description = "학교별 지원 가능 총원과 차수별 지원 인원")
        List<ApplicationSchoolStatisticsResponse> schoolApplicationStatistics,
        @Schema(description = "프로젝트별 차수별 지원 인원")
        List<ApplicationProjectStatisticsResponse> projectRoundStatistics
    ) {
        private static ApplicationSummaryResponse from(ApplicationSummaryInfo info) {
            return new ApplicationSummaryResponse(
                info.roundApplicationStatistics().stream()
                    .map(RoundApplicationStatisticsResponse::from)
                    .toList(),
                info.schoolApplicationStatistics().stream()
                    .map(ApplicationSchoolStatisticsResponse::from)
                    .toList(),
                info.projectRoundStatistics().stream()
                    .map(ApplicationProjectStatisticsResponse::from)
                    .toList()
            );
        }
    }

    @Schema(name = "ProjectApplicationSchoolStatisticsResponse", description = "학교별 지원 현황")
    public record ApplicationSchoolStatisticsResponse(
        @Schema(description = "학교 ID")
        Long schoolId,
        @Schema(description = "학교명")
        String schoolName,
        @Schema(description = "지원 가능 총원. 차수가 지나도 차감하지 않습니다.")
        long totalMemberCount,
        @Schema(description = "차수별 지원 인원 수")
        List<ApplicationPhaseCountResponse> rounds
    ) {
        private static ApplicationSchoolStatisticsResponse from(ApplicationSchoolStatisticsInfo info) {
            return new ApplicationSchoolStatisticsResponse(
                info.schoolId(),
                info.schoolName(),
                info.totalMemberCount(),
                info.rounds().stream()
                    .map(ApplicationPhaseCountResponse::from)
                    .toList()
            );
        }
    }

    @Schema(name = "ProjectApplicationProjectStatisticsResponse", description = "프로젝트별 지원 현황")
    public record ApplicationProjectStatisticsResponse(
        @Schema(description = "프로젝트 ID")
        Long projectId,
        @Schema(description = "프로젝트명")
        String projectName,
        @Schema(description = "차수별 지원 인원 수")
        List<ApplicationPhaseCountResponse> rounds
    ) {
        private static ApplicationProjectStatisticsResponse from(ApplicationProjectStatisticsInfo info) {
            return new ApplicationProjectStatisticsResponse(
                info.projectId(),
                info.projectName(),
                info.rounds().stream()
                    .map(ApplicationPhaseCountResponse::from)
                    .toList()
            );
        }
    }

    @Schema(name = "ProjectApplicationPhaseCountResponse", description = "차수별 지원 인원 수")
    public record ApplicationPhaseCountResponse(
        @Schema(description = "매칭 차수")
        MatchingPhase phase,
        @Schema(description = "지원 인원 수")
        long applicantCount
    ) {
        private static ApplicationPhaseCountResponse from(ApplicationPhaseCountInfo info) {
            return new ApplicationPhaseCountResponse(info.phase(), info.applicantCount());
        }
    }
}
