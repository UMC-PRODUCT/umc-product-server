package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.Set;

public record CreateStudyGroupCommand(
        String name,
        ChallengerPart part,
        Long leaderId,
        Set<Long> memberIds
) {
}
