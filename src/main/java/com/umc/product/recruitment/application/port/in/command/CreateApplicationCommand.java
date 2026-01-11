package com.umc.product.recruitment.application.port.in.command;

public record CreateApplicationCommand(
        Long recruitmentId,
        Long applicantMemberId,
        Long formResponseId
) {
}
