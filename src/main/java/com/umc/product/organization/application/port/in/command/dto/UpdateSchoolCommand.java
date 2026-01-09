package com.umc.product.organization.application.port.in.command.dto;

public record UpdateSchoolCommand(
        String schoolName,
        String chapterId,
        String remark
) {

}
