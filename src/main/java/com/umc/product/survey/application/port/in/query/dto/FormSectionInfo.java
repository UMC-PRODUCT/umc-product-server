package com.umc.product.survey.application.port.in.query.dto;

import java.time.Instant;
import lombok.Builder;

/**
 * FormSection 단건 조회 결과 DTO.
 */
@Builder
public record FormSectionInfo(
    Long id,
    Long formId,
    String title,
    String description,
    Long orderNo,
    Instant createdAt,
    Instant updatedAt
) {
}
