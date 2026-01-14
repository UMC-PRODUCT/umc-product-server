package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteRecruitmentCommand(
        Long recruitmentId,
        Long requesterMemberId
) {
}
