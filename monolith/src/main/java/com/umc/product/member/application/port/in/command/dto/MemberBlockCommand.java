package com.umc.product.member.application.port.in.command.dto;

public record MemberBlockCommand(
    Long blockerMemberId,
    Long blockedMemberId
) {

    public MemberBlockCommand {
        blockerMemberId = requirePositive(blockerMemberId, "blockerMemberId");
        blockedMemberId = requirePositive(blockedMemberId, "blockedMemberId");
    }

    private static Long requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
