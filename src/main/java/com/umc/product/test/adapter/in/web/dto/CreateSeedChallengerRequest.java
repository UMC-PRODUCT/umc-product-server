package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerCommand;

import jakarta.validation.constraints.NotNull;

public record CreateSeedChallengerRequest(
    @NotNull(message = "회원 ID는 필수입니다") Long memberId,

    @NotNull(message = "기수 ID는 필수입니다") Long gisuId,

    @NotNull(message = "챌린저 파트는 필수입니다") ChallengerPart part
) {

    public CreateSeedChallengerCommand toCommand() {
        return CreateSeedChallengerCommand.of(memberId, gisuId, part);
    }
}
