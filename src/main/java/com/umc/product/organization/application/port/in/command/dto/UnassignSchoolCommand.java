package com.umc.product.organization.application.port.in.command.dto;

public record UnassignSchoolCommand(
        Long schoolId,
        Long gisuId
) {
}
