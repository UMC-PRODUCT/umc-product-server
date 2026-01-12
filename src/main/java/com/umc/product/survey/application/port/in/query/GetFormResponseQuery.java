package com.umc.product.survey.application.port.in.query;

public record GetFormResponseQuery(
        Long userId,
        Long formResponseId
) {
}
