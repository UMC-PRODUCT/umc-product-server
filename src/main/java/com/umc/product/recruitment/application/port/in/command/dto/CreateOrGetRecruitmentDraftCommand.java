package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateOrGetRecruitmentDraftCommand(
        Long recruitmentId,
        Long memberId
) {
}
