package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberActivitiesCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceProductTeamMemberActivitiesRequest(
    @NotEmpty List<@Valid ProductTeamActivityRequest> activities
) {
    public ReplaceProductTeamMemberActivitiesCommand toCommand(Long productTeamMemberId, Long requesterMemberId) {
        return ReplaceProductTeamMemberActivitiesCommand.of(
            productTeamMemberId,
            requesterMemberId,
            activities.stream().map(ProductTeamActivityRequest::toCommand).toList()
        );
    }
}
