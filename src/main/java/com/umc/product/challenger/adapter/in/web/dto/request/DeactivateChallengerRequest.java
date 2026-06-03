package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationType;
import com.umc.product.challenger.application.port.in.command.dto.DeactivateChallengerCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeactivateChallengerRequest(
        @NotNull(message = "비활성화 타입은 필수입니다")
        ChallengerDeactivationType deactivationType,
        @NotNull(message = "수정자 ID는 필수입니다")
        Long modifiedBy,
        @NotBlank(message = "사유는 필수입니다")
        @Size(max = 200, message = "사유는 200자 이하여야 합니다")
        String reason
) {
    public DeactivateChallengerCommand toCommand(Long challengerId) {
        return DeactivateChallengerCommand.of(
            challengerId,
            deactivationType,
            modifiedBy,
            reason);
    }
}
