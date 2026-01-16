package com.umc.product.organization.application.port.in.command.dto;

public record CreateSchoolCommand(
        String name,
        Long chapterId,
        String remark
) {
}
