package com.umc.product.challenger.application.port.in.command.dto;

import com.umc.product.challenger.domain.enums.PointType;

public record GrantChallengerPointCommand(
        Long challengerId,
        PointType pointType,
        String description
) {
}
