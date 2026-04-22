package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.enums.FormStatus;
import java.time.Instant;
import lombok.Builder;

/**
 * Form 단건 조회 결과 DTO.
 */
@Builder
public record FormInfo(
    Long id,
    Long createdMemberId,
    String title,
    String description,
    FormStatus status,
    boolean isAnonymous,
    Instant createdAt,
    Instant updatedAt
) {
}
