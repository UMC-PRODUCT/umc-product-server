package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberInfo;
import java.util.List;

public record ProductTeamMemberResponse(
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
    List<ProductTeamFunctionalMembershipResponse> functionalMemberships,
    List<ProductTeamSquadParticipationResponse> squadParticipations
) {
    public static ProductTeamMemberResponse from(ProductTeamMemberInfo info) {
        return new ProductTeamMemberResponse(
            info.productTeamMemberId(),
            info.memberId(),
            info.memberName(),
            info.memberNickname(),
            info.memberSchoolName(),
            info.memberProfileImageId(),
            info.memberProfileImageUrl(),
            info.introduction(),
            info.productTeamProfileImageId(),
            info.productTeamProfileImageUrl(),
            info.functionalMemberships().stream().map(ProductTeamFunctionalMembershipResponse::from).toList(),
            info.squadParticipations().stream().map(ProductTeamSquadParticipationResponse::from).toList()
        );
    }
}
