package com.umc.product.organization.application.port.in.dto;

import java.time.LocalDate;

public record UpdateSchoolResult(
    String newSchoolName,
    Long chapterId,
    String chapterName,
    String remark,
    LocalDate createdAt,
    LocalDate updatedAt
) {
}
