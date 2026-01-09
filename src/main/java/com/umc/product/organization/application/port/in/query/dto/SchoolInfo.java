package com.umc.product.organization.application.port.in.query.dto;

import java.time.LocalDate;

public record SchoolInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
