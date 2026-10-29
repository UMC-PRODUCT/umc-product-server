package com.umc.product.project.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;

/**
 * 지부의 공개 프로젝트별 팀원 명단과 모집 정원(TO).
 */
public record ProjectMatchingOverviewInfo(
    List<MatchingOverviewProjectInfo> projects
) {
    public static ProjectMatchingOverviewInfo empty() {
        return new ProjectMatchingOverviewInfo(List.of());
    }

    public record MatchingOverviewProjectInfo(
        Long projectId,
        String projectName,
        long currentMemberCount,
        long quota,
        PartQuotaStatus partQuotaStatus,
        MatchingOverviewOwnerInfo productOwner,
        List<MatchingOverviewOwnerInfo> coProductOwners,
        List<MatchingOverviewPartInfo> parts
    ) {
    }

    public record MatchingOverviewPartInfo(
        ChallengerPart part,
        long currentMemberCount,
        long quota,
        PartQuotaStatus status,
        List<MatchingOverviewMemberInfo> members
    ) {
    }

    public record MatchingOverviewOwnerInfo(Long memberId, String nickname, String name, String schoolName) {
    }

    public record MatchingOverviewMemberInfo(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        MatchingOverviewRoundInfo matchedRoundInfo
    ) {
    }

    public record MatchingOverviewRoundInfo(Long matchingRoundId, MatchingType type, MatchingPhase phase) {
    }
}
