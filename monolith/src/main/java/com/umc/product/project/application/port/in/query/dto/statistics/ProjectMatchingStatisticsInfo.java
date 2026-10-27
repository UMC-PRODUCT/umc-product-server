package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 로그인 사용자에게 공개하는 지부 단위 ProjectMember 기준 매칭 통계.
 */
public record ProjectMatchingStatisticsInfo(
    Long chapterId,
    List<MatchingPhaseStatisticsInfo> roundMatchingStatistics,
    List<MatchingSchoolStatisticsInfo> schoolMatchingStatistics,
    List<MatchingProjectStatisticsInfo> projectMatchingStatistics
) {
    public static ProjectMatchingStatisticsInfo empty(Long chapterId) {
        return new ProjectMatchingStatisticsInfo(chapterId, List.of(), List.of(), List.of());
    }

    public record MatchingPhaseStatisticsInfo(
        MatchingPhase phase,
        long matchedMemberCount,
        long availableMemberCount
    ) {
    }

    public record MatchingSchoolStatisticsInfo(
        Long schoolId,
        String schoolName,
        long matchedMemberCount,
        long totalMemberCount
    ) {
    }

    public record MatchingProjectStatisticsInfo(
        Long projectId,
        String projectName,
        long matchedMemberCount,
        long quota,
        List<MatchingPhaseCountInfo> rounds,
        List<MatchingPartStatisticsInfo> parts
    ) {
    }

    public record MatchingPhaseCountInfo(MatchingPhase phase, long matchedMemberCount) {
    }

    public record MatchingPartStatisticsInfo(ChallengerPart part, long matchedMemberCount, long quota) {
    }
}
