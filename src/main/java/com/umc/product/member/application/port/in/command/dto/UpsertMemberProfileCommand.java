package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.MemberProfileLinkType;
import java.util.List;

public record UpsertMemberProfileCommand(
    Long memberId,
    List<LinkTypeAndLink> links
) {
    public record LinkTypeAndLink(
        MemberProfileLinkType type,
        String link
    ) {
    }
}
