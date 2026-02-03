package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record GetInterviewSheetQuestionsInfo(
        PartKey partKey,
        int questionCount,
        List<InterviewQuestionInfo> questions
) {
    public record InterviewQuestionInfo(Long questionId, Integer orderNo, String questionText) {
    }
}