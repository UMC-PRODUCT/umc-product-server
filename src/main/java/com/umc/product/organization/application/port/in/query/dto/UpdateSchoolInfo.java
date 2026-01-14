package com.umc.product.organization.application.port.in.query.dto;

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
