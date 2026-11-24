package com.umc.product.form.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.form.domain.enums.FormStatus;

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
    boolean allowDuplicateResponses,
    Instant createdAt,
    Instant updatedAt
) {
}
