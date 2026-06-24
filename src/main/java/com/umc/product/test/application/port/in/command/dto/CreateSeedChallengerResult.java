package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record CreateSeedChallengerResult(
    Long challengerId,
    Long memberId,
    Long gisuId,
    ChallengerPart part
) {

    public static CreateSeedChallengerResult of(
        Long challengerId,
        Long memberId,
        Long gisuId,
        ChallengerPart part
    ) {
        return new CreateSeedChallengerResult(challengerId, memberId, gisuId, part);
    }
}
