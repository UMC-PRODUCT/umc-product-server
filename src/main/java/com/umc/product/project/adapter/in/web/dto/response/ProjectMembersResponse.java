package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import java.util.List;
import lombok.Builder;

/**
 * PROJECT-003 응답: 프로젝트의 팀원 구성.
 * <p>
 * 메인 PM 은 별도 필드로 강조 노출. 보조 PM(PLAN 파트의 다른 멤버)은 분리. 그 외 파트는 {@link PartGroup} 으로 묶어 노출.
 * <p>
 * {@link #toPublic()} 호출 시 모든 {@link MemberBrief} 의 실명({@code name})이 마스킹됩니다.
 */
@Builder
public record ProjectMembersResponse(
    Long projectId,
    MemberBrief productOwner,
    List<MemberBrief> coProductOwners,
    List<PartGroup> partGroups
) {

    /** 파트별 멤버 묶음. {@code part} 는 PLAN 외 파트만 등장한다. */
    public record PartGroup(ChallengerPart part, List<MemberBrief> members) {

        public PartGroup toPublic() {
            return new PartGroup(part, members.stream().map(MemberBrief::toPublic).toList());
        }
    }

    public ProjectMembersResponse toPublic() {
        return ProjectMembersResponse.builder()
            .projectId(projectId)
            .productOwner(productOwner == null ? null : productOwner.toPublic())
            .coProductOwners(coProductOwners.stream().map(MemberBrief::toPublic).toList())
            .partGroups(partGroups.stream().map(PartGroup::toPublic).toList())
            .build();
    }
}
