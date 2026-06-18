package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;

public record ReplaceUmcProductMemberFunctionalMembershipsCommand(
    Long umcProductMemberId,
    Long requesterMemberId,
    List<UmcProductFunctionalMembershipCommand> functionalMemberships
) {
    public static ReplaceUmcProductMemberFunctionalMembershipsCommand of(
        Long umcProductMemberId,
        Long requesterMemberId,
        List<UmcProductFunctionalMembershipCommand> functionalMemberships
    ) {
        return new ReplaceUmcProductMemberFunctionalMembershipsCommand(
            umcProductMemberId,
            requesterMemberId,
            functionalMemberships
        );
    }
}
