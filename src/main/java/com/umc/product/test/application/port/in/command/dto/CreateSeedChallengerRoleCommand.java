package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

public record CreateSeedChallengerRoleCommand(
    Long challengerId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {

    public static CreateSeedChallengerRoleCommand of(
        Long challengerId,
        ChallengerRoleType roleType,
        Long organizationId,
        ChallengerPart responsiblePart,
        Long gisuId
    ) {
        return new CreateSeedChallengerRoleCommand(challengerId, roleType, organizationId, responsiblePart, gisuId);
    }
}
