package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerResult;

public record CreateSeedChallengerResponse(
    Long challengerId,
    Long memberId,
    Long gisuId,
    ChallengerPart part
) {

    public static CreateSeedChallengerResponse from(CreateSeedChallengerResult result) {
        return new CreateSeedChallengerResponse(
            result.challengerId(),
            result.memberId(),
            result.gisuId(),
            result.part()
        );
    }
}
