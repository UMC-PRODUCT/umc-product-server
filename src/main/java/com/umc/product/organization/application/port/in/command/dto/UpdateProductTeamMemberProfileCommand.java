package com.umc.product.organization.application.port.in.command.dto;

public record UpdateProductTeamMemberProfileCommand(
    Long productTeamMemberId,
    Long requesterMemberId,
    String introduction,
    String profileImageId
) {
    public static UpdateProductTeamMemberProfileCommand of(
        Long productTeamMemberId,
        Long requesterMemberId,
        String introduction,
        String profileImageId
    ) {
        return new UpdateProductTeamMemberProfileCommand(
            productTeamMemberId,
            requesterMemberId,
            introduction,
            profileImageId
        );
    }
}
