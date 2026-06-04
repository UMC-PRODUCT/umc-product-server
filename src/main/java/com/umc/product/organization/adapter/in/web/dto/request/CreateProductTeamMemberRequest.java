package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamMemberCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateProductTeamMemberRequest(
    @NotNull Long memberId,
    @Size(max = 2000) String introduction,
    String profileImageId,
    @NotEmpty List<@Valid ProductTeamActivityRequest> activities
) {
    public CreateProductTeamMemberCommand toCommand(Long requesterMemberId) {
        return CreateProductTeamMemberCommand.of(
            requesterMemberId,
            memberId,
            introduction,
            profileImageId,
            activities.stream().map(ProductTeamActivityRequest::toCommand).toList()
        );
    }
}
