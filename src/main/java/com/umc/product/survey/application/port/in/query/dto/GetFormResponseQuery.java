package com.umc.product.survey.application.port.in.query.dto;

public record GetFormResponseQuery(
        Long memberId,
        Long formResponseId
) {
}
