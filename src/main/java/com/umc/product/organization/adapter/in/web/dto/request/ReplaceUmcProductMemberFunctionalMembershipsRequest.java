package com.umc.product.organization.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductMemberFunctionalMembershipsCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

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
