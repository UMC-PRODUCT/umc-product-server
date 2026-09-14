package com.umc.product.project.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

import lombok.Builder;

/**
 * PROJECT-003 응답: 프로젝트의 팀원 구성.
 * <p>
 * 메인 PM 은 별도 필드로 강조 노출. 보조 PM(PLAN 파트의 다른 멤버)은 분리. 그 외 파트는 {@link PartGroup} 으로 묶어 노출.
 */
@Builder
public record ProjectMembersResponse(
    Long projectId,
    ProjectMemberBrief productOwner,
    List<ProjectMemberBrief> coProductOwners,
    List<PartGroup> partGroups
) {

    /** 파트별 멤버 묶음. {@code part} 는 PLAN 외 파트만 등장한다. */
    public record PartGroup(ChallengerPart part, List<ProjectMemberBrief> members) {
    }

    /**
     * 프로젝트 팀원 조회 전용 멤버 간략 정보.
     * <p>
     * 공용 {@code MemberBrief} 에 매칭 차수 필드를 섞지 않기 위해 PROJECT-003 응답 안에서만 사용한다.
     */
    @Builder
    public record ProjectMemberBrief(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        MatchedRoundInfo matchedRoundInfo
    ) {
        public static ProjectMemberBrief from(MemberInfo info, MatchedRoundInfo matchedRoundInfo) {
            return ProjectMemberBrief.builder()
                .memberId(info.id())
                .nickname(info.nickname())
                .name(info.name())
                .schoolName(info.schoolName())
                .matchedRoundInfo(matchedRoundInfo)
                .build();
        }
    }

    public record MatchedRoundInfo(
        Long id,
        MatchingType type,
        MatchingPhase phase
    ) {
    }
}
