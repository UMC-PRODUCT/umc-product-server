package com.umc.product.organization.application.port.in.query.dto;

import java.time.Instant;

public record SchoolInfo(
        Long chapterId,
        String chapterName,
        String schoolName,
        Long schoolId,
        String remark,
        String logoImageId,
        Instant createdAt,
        Instant updatedAt
) {
}
