package com.umc.product.command.organization.application.port.in.dto.response;

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
