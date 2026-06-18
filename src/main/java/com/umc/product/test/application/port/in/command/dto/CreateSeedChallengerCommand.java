package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateSeedChallengerCommand(
    Long memberId,
    Long gisuId,
    ChallengerPart part
) {

    public static CreateSeedChallengerCommand of(Long memberId, Long gisuId, ChallengerPart part) {
        return new CreateSeedChallengerCommand(memberId, gisuId, part);
    }
}
