package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.time.Instant;
import java.util.List;

public record DraftFormResponseInfo(
        Long formResponseId,
        Long formId,
        FormResponseStatus status, // 항상 DRAFT
        Instant lastSavedAt,
        List<AnswerInfo> answers,
        Instant createdAt
) {
    public static DraftFormResponseInfo from(FormResponse fr) {
        return new DraftFormResponseInfo(
                fr.getId(),
                fr.getForm().getId(),
                fr.getStatus(),
                fr.getUpdatedAt(),
                fr.getAnswers() == null
                        ? List.of()
                        : fr.getAnswers().stream()
                                .map(AnswerInfo::from)
                                .toList(),
                fr.getCreatedAt()
        );
    }
}
