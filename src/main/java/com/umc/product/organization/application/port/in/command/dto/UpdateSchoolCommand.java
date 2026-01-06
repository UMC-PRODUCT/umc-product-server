package com.umc.product.organization.application.port.in.command.dto;

public record UpdateSchoolCommand(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String remark
) {

}
