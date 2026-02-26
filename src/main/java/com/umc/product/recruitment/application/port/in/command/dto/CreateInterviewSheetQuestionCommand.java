package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;

public record CreateInterviewSheetQuestionCommand(
    Long recruitmentId,
    PartKey partKey,
    String questionText,
    Long requesterMemberId
) {
    public InterviewQuestionSheet toEntity(Recruitment recruitment, Integer orderNo) {
        return InterviewQuestionSheet.builder()
            .recruitment(recruitment)
            .partKey(partKey)
            .orderNo(orderNo)
            .content(questionText)
            .build();
    }
}
