package com.umc.product.challenger.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record UpdateChallengerCommand(
        Long challengerId,
        ChallengerPart newPart
) {
}
