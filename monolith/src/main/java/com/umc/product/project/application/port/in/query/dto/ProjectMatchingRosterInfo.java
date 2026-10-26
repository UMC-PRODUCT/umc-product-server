package com.umc.product.project.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
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
        /** 회원 정보와 선발 차수 정보를 팀원 명단 항목으로 변환한다. 회원 정보가 없으면 null을 반환한다. */
        public static RosterMemberInfo of(
            MemberInfo member, ChallengerPart part, Long roundId, MatchingType type, MatchingPhase phase
        ) {
            if (member == null) {
                return null;
            }
            return new RosterMemberInfo(
                member.id(),
                member.nickname(),
                member.name(),
                member.schoolName(),
                part,
                roundId,
                type,
                phase
            );
        }
    }
}
