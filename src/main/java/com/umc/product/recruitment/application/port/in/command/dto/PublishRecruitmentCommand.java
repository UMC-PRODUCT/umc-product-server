package com.umc.product.recruitment.application.port.in.command.dto;

public record PublishRecruitmentCommand(
    Long recruitmentId,
    Long requesterMemberId,
    UpdateRecruitmentDraftCommand updateRecruitmentDraftCommand,
    UpsertRecruitmentFormQuestionsCommand upsertRecruitmentFormQuestionsCommand
) {
}
