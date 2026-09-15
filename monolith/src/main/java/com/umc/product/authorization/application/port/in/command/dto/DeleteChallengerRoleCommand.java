package com.umc.product.authorization.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record DeleteChallengerRoleCommand(
    Long challengerRoleId
) {

}
