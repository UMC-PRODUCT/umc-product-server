package com.umc.product.authorization.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import lombok.Builder;

@Builder
public record UpdateChallengerRoleCommand(
    Long challengerRoleId,
    ChallengerRoleType roleType,
    Long organizationId,
    ChallengerPart responsiblePart
) {

}