package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record CreateProductTeamMemberCommand(
    Long requesterMemberId,
    Long memberId,
    String introduction,
    String profileImageId,
    List<ProductTeamActivityCommand> activities
) {
    public static CreateProductTeamMemberCommand of(
        Long requesterMemberId,
        Long memberId,
        String introduction,
        String profileImageId,
        List<ProductTeamActivityCommand> activities
    ) {
        return new CreateProductTeamMemberCommand(requesterMemberId, memberId, introduction, profileImageId, activities);
    }
}
