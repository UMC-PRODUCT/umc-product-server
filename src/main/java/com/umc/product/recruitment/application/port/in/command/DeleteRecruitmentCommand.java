package com.umc.product.recruitment.application.port.in.command;

public record DeleteRecruitmentCommand(
        Long recruitmentId,
        Long requesterMemberId
) {
}
