package com.umc.product.project.application.port.in.query.dto.statistics;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 프로젝트별 매칭 완료 인원 수와 모집 정원(TO).
 */
public record ProjectMatchingStatisticsInfo(
    Long projectId,
    String projectName,
    long matchedMemberCount,
    long quota,
    List<PhaseMatchingCountInfo> rounds,
    List<PartMatchingStatisticsInfo> parts
) {
    public record PhaseMatchingCountInfo(MatchingPhase phase, long matchedMemberCount) {
    }

    public record PartMatchingStatisticsInfo(ChallengerPart part, long matchedMemberCount, long quota) {
    }
}
