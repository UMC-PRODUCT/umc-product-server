package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.domain.enums.PartKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInterviewSheetQuestionRequest(
    @NotNull PartKey partKey,
    @NotBlank String questionText
) {
    public CreateInterviewSheetQuestionCommand toCommand(Long recruitmentId, Long requesterMemberId) {
        return new CreateInterviewSheetQuestionCommand(
            recruitmentId,
            partKey,
            questionText,
            requesterMemberId
        );
    }
}
