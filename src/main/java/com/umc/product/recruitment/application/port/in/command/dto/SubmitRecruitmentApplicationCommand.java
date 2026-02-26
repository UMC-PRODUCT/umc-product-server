package com.umc.product.recruitment.application.port.in.command.dto;

public record SubmitRecruitmentApplicationCommand(
    Long recruitmentId,
    Long applicantMemberId,
    Long formResponseId
) {
}
