package com.umc.product.member.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record DeleteMemberCommand(
    Long memberId
) {
}
