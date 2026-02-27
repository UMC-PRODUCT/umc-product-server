package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteRecruitmentQuestionOptionCommand(
    Long recruitmentId,
    Long questionId,
    Long optionId,
    Long memberId
) {
}
