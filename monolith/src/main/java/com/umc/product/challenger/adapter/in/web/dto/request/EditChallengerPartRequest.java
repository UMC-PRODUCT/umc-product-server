package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerPart;

import jakarta.validation.constraints.NotNull;

public record EditChallengerPartRequest(
        @NotNull(message = "변경할 파트는 필수입니다") ChallengerPart newPart
) {
    public UpdateChallengerCommand toCommand(Long challengerId, Long modifiedBy) {
        return UpdateChallengerCommand.forPartChange(
                challengerId,
                this.newPart,
                modifiedBy
        );
    }
}
