package com.umc.product.organization.application.port.in.command.dto;

public record AssignSchoolCommand(
        Long schoolId,
        Long chapterId
) {
}
