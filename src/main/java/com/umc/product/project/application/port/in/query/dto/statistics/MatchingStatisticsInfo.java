package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

/** 운영진 매칭통계 집계 결과. */
public record MatchingStatisticsInfo(
    List<MatchingRoundStatistics> matchingRoundStatistics,
    List<ApplicantSchoolStatistics> applicantSchoolStatistics,
    List<ProjectApplicantStatistics> projectApplicantStatistics
) {
}
