package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;
import jakarta.validation.constraints.Size;

public record UpdateProductTeamMemberProfileRequest(
    @Size(max = 2000) String introduction,
    String profileImageId
) {
    public UpdateProductTeamMemberProfileCommand toCommand(Long productTeamMemberId, Long requesterMemberId) {
        return UpdateProductTeamMemberProfileCommand.of(
            productTeamMemberId,
            requesterMemberId,
            introduction,
            profileImageId
        );
    }
}
