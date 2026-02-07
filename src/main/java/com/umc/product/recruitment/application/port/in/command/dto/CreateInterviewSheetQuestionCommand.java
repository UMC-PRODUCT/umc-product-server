package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.enums.PartKey;

public record CreateInterviewSheetQuestionCommand(
    Long recruitmentId,
    PartKey partKey,
    String questionText,
    Long requesterMemberId
) {
}
