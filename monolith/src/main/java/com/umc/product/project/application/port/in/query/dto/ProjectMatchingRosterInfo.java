package com.umc.product.project.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

/**
 * 프로젝트별 팀원과 선발 차수를 담는 Info DTO.
 */
public record ProjectMatchingRosterInfo(
    Long projectId,
    RosterMemberInfo productOwner,
    List<RosterMemberInfo> members
) {

    public record RosterMemberInfo(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        ChallengerPart part,
        Long matchingRoundId,
        MatchingType matchingType,
        MatchingPhase matchingPhase
    ) {
    }
}
