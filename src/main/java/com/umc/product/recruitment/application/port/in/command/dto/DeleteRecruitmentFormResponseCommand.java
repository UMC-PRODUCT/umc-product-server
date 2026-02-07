package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteRecruitmentFormResponseCommand(
    Long recruitmentId,
    Long formResponseId
) {
}
