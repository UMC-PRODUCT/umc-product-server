package com.umc.product.organization.application.port.in.dto;

import java.time.LocalDate;

public record DeletableSchoolSummary(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String chapterName,
        LocalDate createdAt,
        boolean isActive
) {

}
