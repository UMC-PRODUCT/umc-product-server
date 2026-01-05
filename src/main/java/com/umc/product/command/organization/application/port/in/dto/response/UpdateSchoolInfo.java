package com.umc.product.command.organization.application.port.in.dto.response;

import java.time.LocalDate;

public record UpdateSchoolInfo(
        String newSchoolName,
        Long chapterId,
        String chapterName,
        String remark,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
