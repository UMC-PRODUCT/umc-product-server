package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamMemberCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public record CreateProductTeamMemberRequest(
    @NotNull Long memberId,
    @Size(max = 2000) String introduction,
    String profileImageId,
    @NotEmpty List<@Valid ProductTeamFunctionalMembershipRequest> functionalMemberships,
    List<@Valid ProductTeamSquadParticipationRequest> squadParticipations
) {
    public CreateProductTeamMemberCommand toCommand(Long requesterMemberId) {
        return CreateProductTeamMemberCommand.of(
            requesterMemberId,
            memberId,
            introduction,
            profileImageId,
            functionalMemberships.stream().map(ProductTeamFunctionalMembershipRequest::toCommand).toList(),
            Objects.requireNonNullElse(squadParticipations, List.<ProductTeamSquadParticipationRequest>of())
                .stream()
                .map(ProductTeamSquadParticipationRequest::toCommand)
                .toList()
        );
    }
}
