package com.umc.product.organization.application.port.in.command.dto;

import java.util.Set;

public record UpdateStudyGroupMembersCommand(
        Long groupId,
        Set<Long> challengerIds
) {
}
