package com.umc.product.organization.application.port.in.query.dto.productteam;

import java.util.List;

public record ProductTeamMemberInfo(
    Long productTeamMemberId,
    Long memberId,
    String memberName,
    String memberNickname,
    String memberSchoolName,
    String memberProfileImageId,
    String memberProfileImageUrl,
    String introduction,
    String productTeamProfileImageId,
    String productTeamProfileImageUrl,
    List<ProductTeamFunctionalMembershipInfo> functionalMemberships,
    List<ProductTeamSquadParticipationInfo> squadParticipations
) {
}
