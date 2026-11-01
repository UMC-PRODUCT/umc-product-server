package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 기준 매칭 유형의 차수별·학교별·프로젝트별 지원 통계.
 */
public record ProjectApplicationStatisticsInfo(
    Long chapterId,
    ProjectMatchingRoundStatisticsInfo matchingRound,
    ApplicationSummaryInfo summary
) {
    public static ProjectApplicationStatisticsInfo empty(Long chapterId) {
        return new ProjectApplicationStatisticsInfo(chapterId, null,
            new ApplicationSummaryInfo(List.of(), List.of(), List.of()));
    }

    public record ApplicationSummaryInfo(
        List<RoundApplicationStatisticsInfo> roundApplicationStatistics,
        List<ApplicationSchoolStatisticsInfo> schoolApplicationStatistics,
        List<ApplicationProjectStatisticsInfo> projectRoundStatistics
    ) {
    }

    public record ApplicationSchoolStatisticsInfo(
        Long schoolId,
        String schoolName,
        long totalMemberCount,
        List<ApplicationPhaseCountInfo> rounds
    ) {
    }

    public record ApplicationProjectStatisticsInfo(
        Long projectId,
        String projectName,
        List<ApplicationPhaseCountInfo> rounds
    ) {
    }

    public record ApplicationPhaseCountInfo(MatchingPhase phase, long applicantCount) {
    }
}
