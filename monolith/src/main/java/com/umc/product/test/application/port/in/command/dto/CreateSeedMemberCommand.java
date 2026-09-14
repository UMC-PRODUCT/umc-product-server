package com.umc.product.test.application.port.in.command.dto;

public record CreateSeedMemberCommand(
    String name,
    String nickname,
    Long schoolId,
    String email,
    String rawPassword
) {

    public static CreateSeedMemberCommand of(
        String name,
        String nickname,
        Long schoolId,
        String email,
        String rawPassword
    ) {
        return new CreateSeedMemberCommand(name, nickname, schoolId, email, rawPassword);
    }
}
