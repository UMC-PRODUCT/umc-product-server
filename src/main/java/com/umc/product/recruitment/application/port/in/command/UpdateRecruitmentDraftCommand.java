package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.domain.RecruitmentPhase;

public record UpdateRecruitmentDraftCommand(
        Long recruitmentId,
        Long requesterMemberId,
        String title,
        String description,
        Boolean isActive,
        RecruitmentPhase phase
) {
}
