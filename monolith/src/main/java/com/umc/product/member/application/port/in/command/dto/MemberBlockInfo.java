package com.umc.product.member.application.port.in.command.dto;

public record MemberBlockInfo(
    Long blockerMemberId,
    Long blockedMemberId,
    boolean blocked
) {
}
