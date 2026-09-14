package com.umc.product.member.adapter.in.web.dto.request;

import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand;
import com.umc.product.member.domain.LinkTypeAndLink;
import java.util.List;

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
