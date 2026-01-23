package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteRecruitmentFormQuestionCommand(
        Long recruitmentId,
        Long requesterMemberId,
        Long questionId
) {
}
