package com.umc.product.organization.application.port.in.command.dto;

import java.util.Set;

public record UpdateStudyGroupCommand(
        Long groupId,
        String name,
        Long leaderId,
        Set<Long> memberIds
) {
}
