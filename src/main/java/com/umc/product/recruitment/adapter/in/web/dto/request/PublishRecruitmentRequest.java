package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;
import jakarta.validation.Valid;

public record PublishRecruitmentRequest(
        @Valid UpdateRecruitmentDraftRequest recruitmentDraft,
        @Valid UpsertRecruitmentFormQuestionsRequest applicationFormQuestions
) {
    public static PublishRecruitmentRequest empty() {
        return new PublishRecruitmentRequest(null, null);
    }

    public PublishRecruitmentCommand toCommand(
            Long recruitmentId,
            Long memberId
    ) {
        return new PublishRecruitmentCommand(
                recruitmentId,
                memberId,
                recruitmentDraft == null ? null : recruitmentDraft.toCommand(recruitmentId, memberId),
                applicationFormQuestions == null ? null : applicationFormQuestions.toCommand(recruitmentId)
        );
    }
}
