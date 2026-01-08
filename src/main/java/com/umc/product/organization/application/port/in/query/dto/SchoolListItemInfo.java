package com.umc.product.organization.application.port.in.query.dto;

import java.time.LocalDate;

public record SchoolListItemInfo(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String chapterName,
        LocalDate createdAt,
        boolean isActive
) {

}
