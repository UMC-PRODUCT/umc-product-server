package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerPointCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditChallengerPointRequest(
        @NotBlank(message = "상벌점 설명은 필수입니다") @Size(max = 200, message = "상벌점 설명은 200자 이하여야 합니다") String newDescription
) {
    public UpdateChallengerPointCommand toCommand(Long challengerPointId) {
        return UpdateChallengerPointCommand.of(challengerPointId, newDescription);
    }
}
