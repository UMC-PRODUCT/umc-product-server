package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.enums.QuestionType;
import java.time.Instant;
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
