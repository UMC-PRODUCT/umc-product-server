package com.umc.product.organization.application.port.in.query.dto.school;

import java.time.Instant;

public record UpdateSchoolInfo(
    String newSchoolName,
    Long chapterId,
    String chapterName,
    String remark,
    Instant createdAt,
    Instant updatedAt
) {
}
