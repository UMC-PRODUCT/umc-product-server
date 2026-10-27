package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 기준 매칭 유형의 차수별·학교별·프로젝트별 지원 통계.
 */
public record ProjectApplicationStatisticsInfo(
    Long chapterId,
    ProjectMatchingRoundStatisticsInfo matchingRound,
    SummaryInfo summary
) {
    public static ProjectApplicationStatisticsInfo empty(Long chapterId) {
        return new ProjectApplicationStatisticsInfo(chapterId, null,
            new SummaryInfo(List.of(), List.of(), List.of()));
    }

    public record SummaryInfo(
        List<RoundApplicationStatisticsInfo> roundApplicationStatistics,
        List<SchoolStatisticsInfo> schoolApplicationStatistics,
        List<ProjectStatisticsInfo> projectRoundStatistics
    ) {
    }

    public record SchoolStatisticsInfo(
        Long schoolId,
        String schoolName,
        long totalMemberCount,
        List<PhaseApplicantCountInfo> rounds
    ) {
    }

    public record ProjectStatisticsInfo(
        Long projectId,
        String projectName,
        List<PhaseApplicantCountInfo> rounds
    ) {
    }

    public record PhaseApplicantCountInfo(MatchingPhase phase, long applicantCount) {
    }
}
