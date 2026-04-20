package com.umc.product.organization.application.port.in.command.dto;

public record UpdateStudyGroupCommand(
        Long groupId,
        String name,
        String part
) {
}
