package com.umc.product.survey.application.port.in.query.dto;

import java.time.Instant;
import lombok.Builder;

/**
 * QuestionOption 단건 조회 결과 DTO.
 */
@Builder
public record QuestionOptionInfo(
    Long id,
    Long questionId,
    String content,
    Long orderNo,
    boolean isOther,
    Instant createdAt,
    Instant updatedAt
) {
}
