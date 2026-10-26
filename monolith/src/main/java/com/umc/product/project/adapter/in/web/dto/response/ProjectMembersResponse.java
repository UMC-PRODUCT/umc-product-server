package com.umc.product.project.adapter.in.web.dto.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo.RosterMemberInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

import lombok.Builder;

/**
 * PROJECT-003·PROJECT-007 응답: 프로젝트의 팀원 구성.
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

    /** 팀원 명단을 메인 PM·보조 PM·파트별 팀원으로 나누어 응답을 구성한다. */
    public static ProjectMembersResponse from(ProjectMatchingRosterInfo info) {
        Long ownerId = info.productOwner() == null ? null : info.productOwner().memberId();
        Map<ChallengerPart, List<ProjectMemberBrief>> partToMembers = new EnumMap<>(ChallengerPart.class);
        for (RosterMemberInfo member : info.members()) {
            ProjectMemberBrief brief = ProjectMemberBrief.from(member);
            partToMembers.computeIfAbsent(member.part(), part -> new ArrayList<>()).add(brief);
        }

        List<ProjectMemberBrief> coOwners = partToMembers.getOrDefault(ChallengerPart.PLAN, List.of()).stream()
            .filter(member -> !Objects.equals(member.memberId(), ownerId))
            .toList();
        List<PartGroup> groups = Arrays.stream(ChallengerPart.values())
            .filter(part -> part != ChallengerPart.PLAN)
            .map(part -> new PartGroup(part, partToMembers.getOrDefault(part, List.of())))
            .filter(group -> !group.members().isEmpty())
            .toList();
        return ProjectMembersResponse.builder()
            .projectId(info.projectId())
            .productOwner(ProjectMemberBrief.from(info.productOwner()))
            .coProductOwners(coOwners)
            .partGroups(groups)
            .build();
    }

    /** 파트별 멤버 묶음. {@code part} 는 PLAN 외 파트만 등장한다. */
    public record PartGroup(ChallengerPart part, List<ProjectMemberBrief> members) {
    }

    /**
     * 프로젝트 팀원 조회 전용 멤버 간략 정보.
     * <p>
     * 공용 {@code MemberBrief} 에 매칭 차수 필드를 섞지 않기 위해 팀원 구성 응답 안에서만 사용한다.
     */
    @Builder
    public record ProjectMemberBrief(
        Long memberId,
        String nickname,
        String name,
        String schoolName,
        MatchedRoundInfo matchedRoundInfo
    ) {
        public static ProjectMemberBrief from(RosterMemberInfo info) {
            if (info == null) {
                return null;
            }
            MatchedRoundInfo round = info.matchingRoundId() == null ? null
                : new MatchedRoundInfo(info.matchingRoundId(), info.matchingType(), info.matchingPhase());
            return ProjectMemberBrief.builder()
                .memberId(info.memberId())
                .nickname(info.nickname())
                .name(info.name())
                .schoolName(info.schoolName())
                .matchedRoundInfo(round)
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
