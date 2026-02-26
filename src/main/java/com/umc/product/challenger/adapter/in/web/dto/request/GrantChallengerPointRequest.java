package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.domain.enums.PointType;

public record GrantChallengerPointRequest(
        PointType pointType,
        String description
) {
    public GrantChallengerPointCommand toCommand(Long challengerId) {
        return new GrantChallengerPointCommand(challengerId, pointType, description);
    }
}
