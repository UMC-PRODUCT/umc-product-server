package com.umc.product.recruitment.application.port.in.query.dto;

public record GetLiveQuestionsQuery(
        Long recruitmentId,
        Long assignmentId,
        Long memberId
) {
}