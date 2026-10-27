package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 로그인 사용자에게 공개하는 지부 단위 ProjectMember 기준 매칭 통계.
 */
public record ProjectMatchingStatisticsInfo(
    Long chapterId,
    List<PhaseStatisticsInfo> roundMatchingStatistics,
    List<SchoolStatisticsInfo> schoolMatchingStatistics,
    List<ProjectStatisticsInfo> projectMatchingStatistics
) {
    public static ProjectMatchingStatisticsInfo empty(Long chapterId) {
        return new ProjectMatchingStatisticsInfo(chapterId, List.of(), List.of(), List.of());
    }

    public record PhaseStatisticsInfo(
        MatchingPhase phase,
        long matchedMemberCount,
        long availableMemberCount
    ) {
    }

    public record SchoolStatisticsInfo(
        Long schoolId,
        String schoolName,
        long matchedMemberCount,
        long totalMemberCount
    ) {
    }

    public record ProjectStatisticsInfo(
        Long projectId,
        String projectName,
        long matchedMemberCount,
        long quota,
        List<PhaseMatchingCountInfo> rounds,
        List<PartStatisticsInfo> parts
    ) {
    }

    public record PhaseMatchingCountInfo(MatchingPhase phase, long matchedMemberCount) {
    }

    public record PartStatisticsInfo(ChallengerPart part, long matchedMemberCount, long quota) {
    }
}
