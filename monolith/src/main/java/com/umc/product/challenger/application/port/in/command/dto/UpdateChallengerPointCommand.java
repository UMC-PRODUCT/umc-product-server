package com.umc.product.challenger.application.port.in.command.dto;

public record UpdateChallengerPointCommand(
        Long challengerPointId,
        String newDescription // 설명 수정
) {
    public static UpdateChallengerPointCommand of(Long challengerPointId, String newDescription) {
        return new UpdateChallengerPointCommand(challengerPointId, newDescription);
    }
}
