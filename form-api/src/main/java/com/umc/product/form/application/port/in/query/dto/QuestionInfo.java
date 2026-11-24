package com.umc.product.form.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.form.domain.enums.QuestionType;

import lombok.Builder;

/**
 * Question 단건 조회 결과 DTO.
 */
@Builder
public record QuestionInfo(
    Long id,
    Long sectionId,
    QuestionType type,
    String title,
    String description,
    boolean isRequired,
    Long orderNo,
    Instant createdAt,
    Instant updatedAt
) {
}
