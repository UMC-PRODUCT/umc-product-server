package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.domain.enums.PointType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrantChallengerPointRequest(
    @NotNull(message = "상벌점 타입은 필수입니다") PointType pointType,
    Integer pointValue,
    @Size(max = 200, message = "상벌점 설명은 200자 이하여야 합니다") String description
) {
    public GrantChallengerPointCommand toCommand(Long challengerId) {
        return new GrantChallengerPointCommand(challengerId, pointType, pointValue, description);
    }
}
