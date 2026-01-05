package com.umc.product.organization.application.port.in.dto;

import java.time.LocalDate;

public record SchoolDetailQuery (
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
