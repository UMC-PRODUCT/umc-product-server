package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record UpdateStudyGroupCommand(
        Long groupId,
        String name,
        ChallengerPart part
) {
}
