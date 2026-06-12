package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductMemberCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public record CreateUmcProductMemberRequest(
    @NotNull Long memberId,
    @Size(max = 2000) String introduction,
    String profileImageId,
    @NotEmpty List<@Valid UmcProductFunctionalMembershipRequest> functionalMemberships,
    List<@Valid UmcProductSquadParticipationRequest> squadParticipations
) {
    public CreateUmcProductMemberCommand toCommand(Long requesterMemberId) {
        return CreateUmcProductMemberCommand.of(
            requesterMemberId,
            memberId,
            introduction,
            profileImageId,
            functionalMemberships.stream().map(UmcProductFunctionalMembershipRequest::toCommand).toList(),
            Objects.requireNonNullElse(squadParticipations, List.<UmcProductSquadParticipationRequest>of())
                .stream()
                .map(UmcProductSquadParticipationRequest::toCommand)
                .toList()
        );
    }
}
