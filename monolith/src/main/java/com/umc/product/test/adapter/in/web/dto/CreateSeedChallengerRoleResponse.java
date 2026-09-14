package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleResult;

public record CreateSeedChallengerRoleResponse(
    Long challengerRoleId,
    Long challengerId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart,
    Long gisuId
) {

    public static CreateSeedChallengerRoleResponse from(CreateSeedChallengerRoleResult result) {
        return new CreateSeedChallengerRoleResponse(
            result.challengerRoleId(),
            result.challengerId(),
            result.roleType(),
            result.organizationId(),
            result.responsiblePart(),
            result.gisuId()
        );
    }
}
