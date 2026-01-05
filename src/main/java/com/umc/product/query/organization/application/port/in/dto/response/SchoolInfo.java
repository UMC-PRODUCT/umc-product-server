package com.umc.product.query.organization.application.port.in.dto.response;

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
