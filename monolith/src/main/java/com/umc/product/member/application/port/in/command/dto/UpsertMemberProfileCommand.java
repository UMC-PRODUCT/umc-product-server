package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.LinkTypeAndLink;
import java.util.List;
import lombok.Builder;

@Builder
public record UpsertMemberProfileCommand(
    Long memberId,
    List<LinkTypeAndLink> links
) {
}
