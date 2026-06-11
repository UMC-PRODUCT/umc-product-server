package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberFunctionalMembershipsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceProductTeamMemberFunctionalMembershipsRequest(
    @NotEmpty List<@Valid ProductTeamFunctionalMembershipRequest> functionalMemberships
) {
    public ReplaceProductTeamMemberFunctionalMembershipsCommand toCommand(
        Long productTeamMemberId,
        Long requesterMemberId
    ) {
        return ReplaceProductTeamMemberFunctionalMembershipsCommand.of(
            productTeamMemberId,
            requesterMemberId,
            functionalMemberships.stream().map(ProductTeamFunctionalMembershipRequest::toCommand).toList()
        );
    }
}
