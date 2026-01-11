package com.umc.product.recruitment.application.port.in.command;

public record PublishRecruitmentCommand(
        Long recruitmentId,
        Long requesterMemberId
) {
}
