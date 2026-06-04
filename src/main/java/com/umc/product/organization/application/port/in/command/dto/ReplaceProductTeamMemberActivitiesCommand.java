package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record ReplaceProductTeamMemberActivitiesCommand(
    Long productTeamMemberId,
    Long requesterMemberId,
    List<ProductTeamActivityCommand> activities
) {
    public static ReplaceProductTeamMemberActivitiesCommand of(
        Long productTeamMemberId,
        Long requesterMemberId,
        List<ProductTeamActivityCommand> activities
    ) {
        return new ReplaceProductTeamMemberActivitiesCommand(productTeamMemberId, requesterMemberId, activities);
    }
}
