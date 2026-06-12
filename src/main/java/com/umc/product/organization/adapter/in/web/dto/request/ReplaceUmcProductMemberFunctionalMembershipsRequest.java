package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductMemberFunctionalMembershipsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceUmcProductMemberFunctionalMembershipsRequest(
    @NotEmpty List<@Valid UmcProductFunctionalMembershipRequest> functionalMemberships
) {
    public ReplaceUmcProductMemberFunctionalMembershipsCommand toCommand(
        Long umcProductMemberId,
        Long requesterMemberId
    ) {
        return ReplaceUmcProductMemberFunctionalMembershipsCommand.of(
            umcProductMemberId,
            requesterMemberId,
            functionalMemberships.stream().map(UmcProductFunctionalMembershipRequest::toCommand).toList()
        );
    }
}
