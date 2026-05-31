package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.Form;
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

    public static FormInfo from(Form form) {
        return FormInfo.builder()
            .id(form.getId())
            .createdMemberId(form.getCreatedMemberId())
            .title(form.getTitle())
            .description(form.getDescription())
            .status(form.getStatus())
            .isAnonymous(form.isAnonymous())
            .createdAt(form.getCreatedAt())
            .updatedAt(form.getUpdatedAt())
            .build();
    }
}
