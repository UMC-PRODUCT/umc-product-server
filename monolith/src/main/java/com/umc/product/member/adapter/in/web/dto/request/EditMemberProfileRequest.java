package com.umc.product.member.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand;
import com.umc.product.member.domain.LinkTypeAndLink;

public record EditMemberProfileRequest(
    List<LinkTypeAndLink> links
) {
    public UpsertMemberProfileCommand toCommand(Long memberId) {
        return UpsertMemberProfileCommand.builder()
            .memberId(memberId)
            .links(links)
            .build();
    }
}
