package com.umc.product.organization.application.port.in.command.dto;

public record CreateSchoolCommand(
        String schoolName,
        Long chapterId,
        String remark
) {
}
