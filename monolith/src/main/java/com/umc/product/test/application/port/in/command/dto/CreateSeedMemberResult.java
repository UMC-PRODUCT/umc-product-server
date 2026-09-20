package com.umc.product.test.application.port.in.command.dto;

public record CreateSeedMemberResult(
    Long memberId,
    String email
) {

    public static CreateSeedMemberResult of(Long memberId, String email) {
        return new CreateSeedMemberResult(memberId, email);
    }
}
