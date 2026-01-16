package com.umc.product.organization.application.port.in.query.dto;

import java.time.Instant;

public record SchoolListItemInfo(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String chapterName,
        Instant createdAt,
        boolean isActive
) {

}
