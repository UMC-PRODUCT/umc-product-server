package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

public record CreateSeedChallengerRoleResult(
    Long challengerRoleId,
    Long challengerId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {

    public static CreateSeedChallengerRoleResult of(
        Long challengerRoleId,
        Long challengerId,
        ChallengerRoleType roleType,
        Long organizationId,
        ChallengerPart responsiblePart,
        Long gisuId
    ) {
        return new CreateSeedChallengerRoleResult(
            challengerRoleId,
            challengerId,
            roleType,
            organizationId,
            responsiblePart,
            gisuId
        );
    }
}
