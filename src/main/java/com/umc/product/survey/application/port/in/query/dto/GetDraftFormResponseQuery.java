package com.umc.product.survey.application.port.in.query.dto;

public record GetDraftFormResponseQuery(
        Long memberId,
        Long formId
) {
}
