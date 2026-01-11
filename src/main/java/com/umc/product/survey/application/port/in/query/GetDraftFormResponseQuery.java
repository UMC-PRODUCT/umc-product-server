package com.umc.product.survey.application.port.in.query;

public record GetDraftFormResponseQuery(
        Long userId,
        Long formId
) {
}
